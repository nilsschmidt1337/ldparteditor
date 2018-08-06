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
package org.nschmidt.ldparteditor.state;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.ScalableComposite;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enums.ManipulatorAxisMode;
import org.nschmidt.ldparteditor.enums.MergeTo;
import org.nschmidt.ldparteditor.enums.MouseButton;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.TextTask;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.enums.WorkingMode;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.KeyBoardHelper;
import org.nschmidt.ldparteditor.helpers.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.helpers.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helpers.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.widgets.NButton;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Stores information about keys pressed on the keyboard and triggers keyboard
 * events.
 *
 * @author nils
 *
 */
public class KeyStateManager {

    /** The 3D Composite */
    private final Composite3D c3d;

    private final CompositePrimitive cp;

    /** A set of all keys which are pressed at the moment */
    private HashSet<Integer> pressedKeyCodes = new HashSet<Integer>();
    /** A map which assigns a pressed key to a task */
    private static final HashMap<String, Task> taskMap = new HashMap<String, Task>();
    private static final HashMap<Task, String> taskKeyMap = new HashMap<Task, String>();
    /** A set of all reserved keys */
    private static final HashSet<String> reservedKeyCodes = new HashSet<String>();
    /** A map which assigns a pressed key to a task (for the text editor)*/
    private static final HashMap<String, TextTask> textTaskMap = new HashMap<String, TextTask>();
    private static final HashMap<TextTask, String> textTaskKeyMap = new HashMap<TextTask, String>();

    private int multi = 100;
    private int colourNumber = 0;

    public static int tmp_stateMask = 0;
    public static int tmp_keyCode = 0;
    public static String tmp_keyString = ""; //$NON-NLS-1$
    public static String tmp_mapKey = ""; //$NON-NLS-1$

    static {

        reservedKeyCodes.add(SWT.ALT + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.CTRL + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.SHIFT + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.COMMAND + ""); //$NON-NLS-1$

        reservedKeyCodes.add(SWT.ARROW_UP + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.ARROW_RIGHT + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.ARROW_DOWN + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.ARROW_LEFT + ""); //$NON-NLS-1$

        reservedKeyCodes.add(SWT.ARROW_UP + "+Alt"); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.ARROW_RIGHT + "+Alt"); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.ARROW_DOWN + "+Alt"); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.ARROW_LEFT + "+Alt"); //$NON-NLS-1$

        reservedKeyCodes.add(SWT.KEYPAD_0 + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.KEYPAD_1 + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.KEYPAD_2 + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.KEYPAD_3 + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.KEYPAD_4 + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.KEYPAD_5 + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.KEYPAD_6 + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.KEYPAD_7 + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.KEYPAD_8 + ""); //$NON-NLS-1$
        reservedKeyCodes.add(SWT.KEYPAD_9 + ""); //$NON-NLS-1$

        addTask(Task.COLOUR_NUMBER0, SWT.KEYPAD_0);
        addTask(Task.COLOUR_NUMBER1, SWT.KEYPAD_1);
        addTask(Task.COLOUR_NUMBER2, SWT.KEYPAD_2);
        addTask(Task.COLOUR_NUMBER3, SWT.KEYPAD_3);
        addTask(Task.COLOUR_NUMBER4, SWT.KEYPAD_4);
        addTask(Task.COLOUR_NUMBER5, SWT.KEYPAD_5);
        addTask(Task.COLOUR_NUMBER6, SWT.KEYPAD_6);
        addTask(Task.COLOUR_NUMBER7, SWT.KEYPAD_7);
        addTask(Task.COLOUR_NUMBER8, SWT.KEYPAD_8);
        addTask(Task.COLOUR_NUMBER9, SWT.KEYPAD_9);

        addTask(Task.TRANSLATE_UP, SWT.ALT, SWT.ARROW_UP);
        addTask(Task.TRANSLATE_RIGHT, SWT.ALT, SWT.ARROW_RIGHT);
        addTask(Task.TRANSLATE_DOWN, SWT.ALT, SWT.ARROW_DOWN);
        addTask(Task.TRANSLATE_LEFT, SWT.ALT, SWT.ARROW_LEFT);

        addTask(Task.TRANSFORM_UP, SWT.ARROW_UP);
        addTask(Task.TRANSFORM_RIGHT, SWT.ARROW_RIGHT);
        addTask(Task.TRANSFORM_DOWN, SWT.ARROW_DOWN);
        addTask(Task.TRANSFORM_LEFT, SWT.ARROW_LEFT);

        if (Cocoa.isCocoa) {

            reservedKeyCodes.add(SWT.ARROW_UP + "+Cmd"); //$NON-NLS-1$
            reservedKeyCodes.add(SWT.ARROW_RIGHT + "+Cmd"); //$NON-NLS-1$
            reservedKeyCodes.add(SWT.ARROW_DOWN + "+Cmd"); //$NON-NLS-1$
            reservedKeyCodes.add(SWT.ARROW_LEFT + "+Cmd"); //$NON-NLS-1$

            reservedKeyCodes.add(SWT.BS + "+Cmd"); //$NON-NLS-1$
            reservedKeyCodes.add((int) 'x' + "+Cmd"); //$NON-NLS-1$
            reservedKeyCodes.add((int) 'c' + "+Cmd"); //$NON-NLS-1$
            reservedKeyCodes.add((int) 'v' + "+Cmd"); //$NON-NLS-1$

            addTask(Task.TRANSFORM_UP_COPY, SWT.COMMAND, SWT.ARROW_UP);
            addTask(Task.TRANSFORM_RIGHT_COPY, SWT.COMMAND, SWT.ARROW_RIGHT);
            addTask(Task.TRANSFORM_DOWN_COPY, SWT.COMMAND, SWT.ARROW_DOWN);
            addTask(Task.TRANSFORM_LEFT_COPY, SWT.COMMAND, SWT.ARROW_LEFT);

            addTask(Task.DELETE, SWT.COMMAND, SWT.BS);
            addTask(Task.ESC, SWT.ESC);
            addTask(Task.COPY, SWT.COMMAND, 'c');
            addTask(Task.CUT, SWT.COMMAND, 'x');
            addTask(Task.PASTE, SWT.COMMAND, 'v');

            addTask(Task.OBJ_VERTEX, SWT.F2);
            addTask(Task.OBJ_FACE, SWT.F3);
            addTask(Task.OBJ_LINE, SWT.F4);
            addTask(Task.OBJ_PRIMITIVE, SWT.F5);

            addTask(Task.MODE_SELECT, '1');
            addTask(Task.MODE_MOVE, '2');
            addTask(Task.MODE_ROTATE,'3');
            addTask(Task.MODE_SCALE, '4');
            addTask(Task.MODE_COMBINED, 'c');

            addTask(Task.MOVE_TO_AVG, 'a');
            addTask(Task.MOVE_ADJACENT_DATA, 'b');

            addTask(Task.SWAP_WINDING, 'j');

            addTask(Task.ADD_VERTEX, '5');
            addTask(Task.ADD_TRIANGLE, '6');
            addTask(Task.ADD_QUAD, '7');
            addTask(Task.ADD_LINE, '8');
            addTask(Task.ADD_CONDLINE, '9');
            addTask(Task.ADD_DISTANCE, 'd');
            addTask(Task.ADD_PROTRACTOR, 'p');
            addTask(Task.ADD_COMMENTS, '0');

            addTask(Task.ZOOM_IN, '+');
            addTask(Task.ZOOM_OUT, '-');
            addTask(Task.RESET_VIEW, SWT.COMMAND, 'r');
            addTask(Task.SHOW_GRID, SWT.COMMAND, 'g');
            addTask(Task.SHOW_RULER, SWT.COMMAND, 'l');

            addTask(Task.UNDO, SWT.COMMAND, 'z');
            addTask(Task.REDO, SWT.COMMAND, 'y');

            addTask(Task.SAVE, SWT.COMMAND, 's');

            addTask(Task.SELECT_ALL, SWT.COMMAND, 'a');
            addTask(Task.SELECT_NONE, SWT.COMMAND | SWT.SHIFT, 'a');
            addTask(Task.SELECT_ALL_WITH_SAME_COLOURS, SWT.COMMAND | SWT.ALT,  'c');
            addTask(Task.SELECT_CONNECTED, SWT.COMMAND, 'j');
            addTask(Task.SELECT_TOUCHING, SWT.COMMAND, 't');

            addTask(Task.SELECT_OPTION_WITH_SAME_COLOURS, SWT.COMMAND | SWT.SHIFT, 's');

            addTask(Task.FLIP_ROTATE_VERTICES, 'f');
            addTask(Task.MERGE_TO_AVERAGE, SWT.COMMAND, 'w');
            addTask(Task.MERGE_TO_LAST, SWT.COMMAND, 'e');
            addTask(Task.SPLIT, SWT.ALT, 'v');

            addTask(Task.LMB, 'k');
            addTask(Task.MMB, SWT.COMMAND);
            addTask(Task.RMB, SWT.ALT);

            addTask(Task.INSERT_AT_CURSOR, 'i');

            addTask(Task.MODE_X, SWT.F6);
            addTask(Task.MODE_Y, SWT.F7);
            addTask(Task.MODE_Z, SWT.F8);
            addTask(Task.MODE_XY, SWT.F9);
            addTask(Task.MODE_XZ, SWT.F10);
            addTask(Task.MODE_YZ, SWT.F11);
            addTask(Task.MODE_XYZ, SWT.F12);

            addTask(TextTask.EDITORTEXT_REPLACE_VERTEX, SWT.COMMAND | SWT.SHIFT, 'r');
            addTask(TextTask.EDITORTEXT_ESC, SWT.ESC);
            addTask(TextTask.EDITORTEXT_QUICKFIX, SWT.COMMAND, 'q');
            addTask(TextTask.EDITORTEXT_SELECTALL, SWT.COMMAND, 'a');
            addTask(TextTask.EDITORTEXT_INLINE, SWT.COMMAND | SWT.SHIFT, 'i');
            addTask(TextTask.EDITORTEXT_ROUND, SWT.COMMAND | SWT.SHIFT, 'c');

            addTask(TextTask.EDITORTEXT_UNDO, SWT.COMMAND, 'z');
            addTask(TextTask.EDITORTEXT_REDO, SWT.COMMAND, 'y');

            addTask(TextTask.EDITORTEXT_SAVE, SWT.COMMAND, 's');

            addTask(TextTask.EDITORTEXT_FIND, SWT.COMMAND, 'f');

            addTask(TextTask.EDITORTEXT_INSERT_HISTORY, SWT.COMMAND, 'l');
            addTask(TextTask.EDITORTEXT_INSERT_KEYWORD, SWT.COMMAND, 'k');
            addTask(TextTask.EDITORTEXT_INSERT_REFERENCE, SWT.COMMAND, 'r');

            addTask(TextTask.EDITORTEXT_LINE_UP, SWT.ALT, SWT.ARROW_UP);
            addTask(TextTask.EDITORTEXT_LINE_DOWN, SWT.ALT, SWT.ARROW_DOWN);

            addTask(Task.CLOSE_VIEW, 'q');

        } else {

            reservedKeyCodes.add(SWT.ARROW_UP + "+Ctrl"); //$NON-NLS-1$
            reservedKeyCodes.add(SWT.ARROW_RIGHT + "+Ctrl"); //$NON-NLS-1$
            reservedKeyCodes.add(SWT.ARROW_DOWN + "+Ctrl"); //$NON-NLS-1$
            reservedKeyCodes.add(SWT.ARROW_LEFT + "+Ctrl"); //$NON-NLS-1$

            reservedKeyCodes.add(SWT.DEL + ""); //$NON-NLS-1$
            reservedKeyCodes.add((int) 'x' + "+Ctrl"); //$NON-NLS-1$
            reservedKeyCodes.add((int) 'c' + "+Ctrl"); //$NON-NLS-1$
            reservedKeyCodes.add((int) 'v' + "+Ctrl"); //$NON-NLS-1$

            addTask(Task.TRANSFORM_UP_COPY, SWT.CTRL, SWT.ARROW_UP);
            addTask(Task.TRANSFORM_RIGHT_COPY, SWT.CTRL, SWT.ARROW_RIGHT);
            addTask(Task.TRANSFORM_DOWN_COPY, SWT.CTRL, SWT.ARROW_DOWN);
            addTask(Task.TRANSFORM_LEFT_COPY, SWT.CTRL, SWT.ARROW_LEFT);

            addTask(Task.DELETE, SWT.DEL);
            addTask(Task.ESC, SWT.ESC);
            addTask(Task.COPY, SWT.CTRL, 'c');
            addTask(Task.CUT, SWT.CTRL, 'x');
            addTask(Task.PASTE, SWT.CTRL, 'v');

            addTask(Task.OBJ_VERTEX, SWT.F2);
            addTask(Task.OBJ_FACE, SWT.F3);
            addTask(Task.OBJ_LINE, SWT.F4);
            addTask(Task.OBJ_PRIMITIVE, SWT.F5);

            addTask(Task.MODE_SELECT, '1');
            addTask(Task.MODE_MOVE, '2');
            addTask(Task.MODE_ROTATE,'3');
            addTask(Task.MODE_SCALE, '4');
            addTask(Task.MODE_COMBINED, 'c');

            addTask(Task.MOVE_TO_AVG, 'a');
            addTask(Task.MOVE_ADJACENT_DATA, 'b');

            addTask(Task.SWAP_WINDING, 'j');

            addTask(Task.ADD_VERTEX, '5');
            addTask(Task.ADD_TRIANGLE, '6');
            addTask(Task.ADD_QUAD, '7');
            addTask(Task.ADD_LINE, '8');
            addTask(Task.ADD_CONDLINE, '9');
            addTask(Task.ADD_DISTANCE, 'd');
            addTask(Task.ADD_PROTRACTOR, 'p');
            addTask(Task.ADD_COMMENTS, '0');

            addTask(Task.ZOOM_IN, '+');
            addTask(Task.ZOOM_OUT, '-');
            addTask(Task.RESET_VIEW, SWT.CTRL, 'r');
            addTask(Task.SHOW_GRID, SWT.CTRL, 'g');
            addTask(Task.SHOW_RULER, SWT.CTRL, 'l');

            addTask(Task.UNDO, SWT.CTRL, 'z');
            addTask(Task.REDO, SWT.CTRL, 'y');

            addTask(Task.SAVE, SWT.CTRL, 's');

            addTask(Task.SELECT_ALL, SWT.CTRL, 'a');
            addTask(Task.SELECT_NONE, SWT.CTRL | SWT.SHIFT, 'a');
            addTask(Task.SELECT_ALL_WITH_SAME_COLOURS, SWT.CTRL | SWT.ALT, 'c');
            addTask(Task.SELECT_CONNECTED, SWT.ALT, 'c');
            addTask(Task.SELECT_TOUCHING, SWT.ALT, 't');

            addTask(Task.SELECT_OPTION_WITH_SAME_COLOURS, SWT.ALT, 's');

            addTask(Task.FLIP_ROTATE_VERTICES, 'f');
            addTask(Task.MERGE_TO_AVERAGE, SWT.CTRL, 'w');
            addTask(Task.MERGE_TO_LAST, SWT.CTRL, 'e');
            addTask(Task.SPLIT, SWT.ALT, 'v');

            addTask(Task.LMB, 'k');
            addTask(Task.MMB, 'm');
            addTask(Task.RMB, 'l');

            addTask(Task.INSERT_AT_CURSOR, 'i');

            addTask(Task.MODE_X, SWT.F6);
            addTask(Task.MODE_Y, SWT.F7);
            addTask(Task.MODE_Z, SWT.F8);
            addTask(Task.MODE_XY, SWT.F9);
            addTask(Task.MODE_XZ, SWT.F10);
            addTask(Task.MODE_YZ, SWT.F11);
            addTask(Task.MODE_XYZ, SWT.F12);

            addTask(TextTask.EDITORTEXT_REPLACE_VERTEX, SWT.ALT | SWT.SHIFT, 'r');
            addTask(TextTask.EDITORTEXT_ESC, SWT.ESC);
            addTask(TextTask.EDITORTEXT_QUICKFIX, SWT.ALT, 'f');
            addTask(TextTask.EDITORTEXT_SELECTALL, SWT.CTRL, 'a');
            addTask(TextTask.EDITORTEXT_INLINE, SWT.ALT, 'i');
            addTask(TextTask.EDITORTEXT_ROUND, SWT.ALT, 'c');

            addTask(TextTask.EDITORTEXT_UNDO, SWT.CTRL, 'z');
            addTask(TextTask.EDITORTEXT_REDO, SWT.CTRL, 'y');

            addTask(TextTask.EDITORTEXT_SAVE, SWT.CTRL, 's');

            addTask(TextTask.EDITORTEXT_FIND, SWT.CTRL, 'f');

            addTask(TextTask.EDITORTEXT_INSERT_HISTORY, SWT.CTRL, 'h');
            addTask(TextTask.EDITORTEXT_INSERT_KEYWORD, SWT.CTRL, 'k');
            addTask(TextTask.EDITORTEXT_INSERT_REFERENCE, SWT.CTRL, 'r');

            addTask(TextTask.EDITORTEXT_LINE_UP, SWT.ALT, SWT.ARROW_UP);
            addTask(TextTask.EDITORTEXT_LINE_DOWN, SWT.ALT, SWT.ARROW_DOWN);

            addTask(Task.CLOSE_VIEW, 'q');
        }
    }

    /** Indicates that SHIFT is pressed */
    private boolean shiftPressed;
    /** Indicates that CTRL is pressed */
    private boolean ctrlPressed;
    /** Indicates that ALT is pressed */
    private boolean altPressed;
    /** Indicates that COMMAND is pressed */
    private boolean cmdPressed;

    public KeyStateManager(Composite3D c3d) {
        this.c3d = c3d;
        this.cp = null;
    }

    public KeyStateManager(CompositePrimitive cp) {
        this.c3d = null;
        this.cp = cp;
    }

    /**
     * @return {@code true} if SHIFT is pressed
     */
    public boolean isShiftPressed() {
        return shiftPressed;
    }

    /**
     * @return {@code true} if CTRL is pressed
     */
    public boolean isCtrlPressed() {
        return ctrlPressed;
    }

    /**
     * @return {@code true} if ALT is pressed
     */
    public boolean isAltPressed() {
        return altPressed;
    }

    /**
     * @return {@code true} if COMMAND is pressed
     */
    public boolean isCmdPressed() {
        return cmdPressed;
    }

    /**
     * Sets the state of released and pressed keys and triggers eventually a
     * function
     *
     * @param keyCode
     *            the Key Code
     * @param keyEventType
     *            the event type can be {@code SWT.KeyDown} or {@code SWT.KeyUp}
     */
    public void setStates(int keyCode, int keyEventType, Event event) {
        if (cp == null) {
            // Logic for Composite3D
            if (keyEventType == SWT.KeyDown && !pressedKeyCodes.contains(keyCode)) {
                NLogger.debug(KeyStateManager.class, "[Key ({0}) down]", keyCode); //$NON-NLS-1$
                setKeyState(keyCode, true);
                pressedKeyCodes.add(keyCode);
                final boolean ctrlPressed = (event.stateMask & SWT.CTRL) != 0;
                final boolean altPressed = (event.stateMask & SWT.ALT) != 0;
                final boolean shiftPressed = (event.stateMask & SWT.SHIFT) != 0;
                final boolean cmdPressed = (event.stateMask & SWT.COMMAND) != 0;
                final StringBuilder sb = new StringBuilder();
                sb.append(keyCode);
                sb.append(ctrlPressed ? "+Ctrl" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(altPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(shiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(cmdPressed ? "+Cmd" : ""); //$NON-NLS-1$//$NON-NLS-2$
                final String key = sb.toString();
                final Task t = taskMap.get(key);
                if (t != null) {
                    boolean hasNumber = false;
                    final DatFile df = c3d.getLockableDatFileReference();
                    final Editor3DWindow win = Editor3DWindow.getWindow();
                    final VertexManager vm = df.getVertexManager();
                    vm.addSnapshot();
                    switch (t) {
                    case DELETE:
                        vm.delete(win.isMovingAdjacentData(), true);
                        break;
                    case INSERT_AT_CURSOR:
                        win.toggleInsertAtCursor();
                        break;
                    case ESC:
                        multi = 100;
                        colourNumber = 0;
                        if (df.getObjVertex1() == null && df.getObjVertex2() == null && df.getObjVertex3() == null && df.getObjVertex4() == null) {
                            win.disableAddAction();
                        }
                        win.activateAllTypes();
                        df.setObjVertex1(null);
                        df.setObjVertex2(null);
                        df.setObjVertex3(null);
                        df.setObjVertex4(null);
                        df.setNearestObjVertex1(null);
                        df.setNearestObjVertex2(null);
                        win.setWorkingLayer(ManipulatorAxisMode.NONE);
                        win.updateInitialScale(BigDecimal.ZERO, BigDecimal.ONE, true);
                        vm.clearSelection();
                        vm.resetSlantingMatrixProjector();
                        break;
                    case COPY:
                        vm.copy();
                        break;
                    case CUT:
                        vm.copy();
                        vm.delete(win.isMovingAdjacentData(), true);
                        break;
                    case PASTE:
                        vm.paste(win.loadSelectorSettings());
                        if (WorkbenchManager.getUserSettingState().isDisableMAD3D()) {
                            win.setMovingAdjacentData(false);
                            GuiStatusManager.updateStatus();
                        }
                        break;
                    case MODE_COMBINED:
                        c3d.getManipulator().applyTranslation(c3d);
                        c3d.getMouse().checkSyncEditMode(vm, df);
                        win.setWorkingAction(WorkingMode.COMBINED);
                        win.disableAddAction();
                        break;
                    case MODE_MOVE:
                        c3d.getManipulator().applyTranslation(c3d);
                        c3d.getMouse().checkSyncEditMode(vm, df);
                        win.setWorkingAction(WorkingMode.MOVE);
                        win.disableAddAction();
                        break;
                    case MODE_ROTATE:
                        c3d.getManipulator().applyTranslation(c3d);
                        c3d.getMouse().checkSyncEditMode(vm, df);
                        win.setWorkingAction(WorkingMode.ROTATE);
                        win.disableAddAction();
                        break;
                    case MODE_SCALE:
                        c3d.getManipulator().applyTranslation(c3d);
                        c3d.getMouse().checkSyncEditMode(vm, df);
                        win.setWorkingAction(WorkingMode.SCALE);
                        win.disableAddAction();
                        break;
                    case MODE_SELECT:
                        c3d.getManipulator().applyTranslation(c3d);
                        c3d.getMouse().checkSyncEditMode(vm, df);
                        win.setWorkingAction(WorkingMode.SELECT);
                        win.disableAddAction();
                        break;
                    case MOVE_TO_AVG:
                        win.mntm_Manipulator_2_toAverage();
                        break;
                    case MOVE_ADJACENT_DATA:
                        win.toggleMoveAdjacentData();
                        break;
                    case SWAP_WINDING:
                        vm.backupHideShowState();
                        vm.windingChangeSelection(true);
                        break;
                    case COLOUR_NUMBER0:

                        hasNumber = true;
                    case COLOUR_NUMBER1:
                        if (!hasNumber) colourNumber += multi;
                        hasNumber = true;
                    case COLOUR_NUMBER2:
                        if (!hasNumber) colourNumber += multi * 2;
                        hasNumber = true;
                    case COLOUR_NUMBER3:
                        if (!hasNumber) colourNumber += multi * 3;
                        hasNumber = true;
                    case COLOUR_NUMBER4:
                        if (!hasNumber) colourNumber += multi * 4;
                        hasNumber = true;
                    case COLOUR_NUMBER5:
                        if (!hasNumber) colourNumber += multi * 5;
                        hasNumber = true;
                    case COLOUR_NUMBER6:
                        if (!hasNumber) colourNumber += multi * 6;
                        hasNumber = true;
                    case COLOUR_NUMBER7:
                        if (!hasNumber) colourNumber += multi * 7;
                        hasNumber = true;
                    case COLOUR_NUMBER8:
                        if (!hasNumber) colourNumber += multi * 8;
                        hasNumber = true;
                    case COLOUR_NUMBER9:
                        if (!hasNumber) colourNumber += multi * 9;
                        multi /= 10;
                        if (multi == 0) {
                            multi = 100;
                            if (View.hasLDConfigColour(colourNumber)) {
                                win.setLastUsedColour2(View.getLDConfigColour(colourNumber));
                            }
                            colourNumber = 0;
                        }
                        break;
                    case ADD_COMMENTS:
                        win.setAddState(0);
                        break;
                    case ADD_DISTANCE:
                        win.setAddState(6);
                        break;
                    case ADD_PROTRACTOR:
                        win.setAddState(7);
                        break;
                    case ADD_CONDLINE:
                        win.setAddState(5);
                        break;
                    case ADD_LINE:
                        win.setAddState(2);
                        break;
                    case ADD_QUAD:
                        win.setAddState(4);
                        break;
                    case ADD_TRIANGLE:
                        win.setAddState(3);
                        break;
                    case ADD_VERTEX:
                        win.setAddState(1);
                        break;
                    case ZOOM_IN:
                        c3d.getPerspectiveCalculator().zoomIn();
                        break;
                    case ZOOM_OUT:
                        c3d.getPerspectiveCalculator().zoomOut();
                        break;
                    case RESET_VIEW:
                        c3d.getPerspectiveCalculator().zoomReset();
                        break;
                    case OBJ_FACE:
                        win.setObjMode(1);
                        break;
                    case OBJ_LINE:
                        win.setObjMode(2);
                        break;
                    case OBJ_PRIMITIVE:
                        win.setObjMode(3);
                        break;
                    case OBJ_VERTEX:
                        win.setObjMode(0);
                        break;
                    case LMB:
                    {
                        Composite3D lc3d = DatFile.getLastHoveredComposite();
                        if (lc3d == null) lc3d = c3d;
                        lc3d.getCanvas().forceFocus();
                        Event mouseEvent = new Event();
                        mouseEvent.type = SWT.MouseDown;
                        mouseEvent.button = MouseButton.LEFT;
                        Vector2f mpos = lc3d.getMousePosition();
                        mouseEvent.x = (int) mpos.x;
                        mouseEvent.y = (int) mpos.y;
                        lc3d.getMouse().mouseDown(mouseEvent);
                        break;
                    }
                    case RMB:
                    {
                        if (!Cocoa.isCocoa) {
                            Composite3D lc3d = DatFile.getLastHoveredComposite();
                            if (lc3d == null) lc3d = c3d;
                            lc3d.getCanvas().forceFocus();
                            Event mouseEvent = new Event();
                            mouseEvent.type = SWT.MouseDown;
                            mouseEvent.button = MouseButton.RIGHT;
                            Vector2f mpos = lc3d.getMousePosition();
                            mouseEvent.x = (int) mpos.x;
                            mouseEvent.y = (int) mpos.y;
                            lc3d.getMouse().mouseDown(mouseEvent);
                        }
                        break;
                    }
                    case MMB:
                    {
                        Composite3D lc3d = DatFile.getLastHoveredComposite();
                        if (lc3d == null) lc3d = c3d;
                        lc3d.getCanvas().forceFocus();
                        Event mouseEvent = new Event();
                        mouseEvent.type = SWT.MouseDown;
                        mouseEvent.button = MouseButton.MIDDLE;
                        Vector2f mpos = lc3d.getMousePosition();
                        mouseEvent.x = (int) mpos.x;
                        mouseEvent.y = (int) mpos.y;
                        lc3d.getMouse().mouseDown(mouseEvent);
                        lc3d.getCanvas().getDisplay().post(mouseEvent);
                        break;
                    }
                    case REDO:
                        df.redo(null, false);
                        pressedKeyCodes.remove(keyCode);
                        break;
                    case UNDO:
                        df.undo(null, false);
                        pressedKeyCodes.remove(keyCode);
                        break;
                    case SAVE:
                        if (!df.isReadOnly()) {
                            if (df.save()) {
                                Editor3DWindow.getWindow().addRecentFile(df);
                                Project.removeUnsavedFile(df);
                                Editor3DWindow.getWindow().updateTree_unsavedEntries();
                                pressedKeyCodes.remove(keyCode);
                            } else {
                                MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_Error);
                                messageBoxError.setMessage(I18n.DIALOG_CantSaveFile);
                                messageBoxError.open();
                            }
                        }
                        break;
                    case SELECT_ALL:
                        vm.selectAll(win.loadSelectorSettings(), true);
                        vm.syncWithTextEditors(true);
                        break;
                    case SELECT_ALL_WITH_SAME_COLOURS:
                        vm.selectAllWithSameColours(win.loadSelectorSettings(), true);
                        vm.syncWithTextEditors(true);
                        break;
                    case SELECT_NONE:
                        vm.clearSelection();
                        vm.syncWithTextEditors(true);
                        break;
                    case SHOW_GRID:
                        if (c3d.getMenu().isDisposed()) {
                            break;
                        }
                        c3d.setGridShown(!c3d.isGridShown());
                        c3d.getMntmShowGrid().setSelection(c3d.isGridShown());
                        break;
                    case SHOW_RULER:
                    {
                        if (c3d.getMenu().isDisposed()) {
                            break;
                        }
                        boolean scale = !c3d.getMntmShowScale().getSelection();
                        c3d.getMntmShowScale().setSelection(scale);
                        c3d.getModifier().showScale(scale);
                        c3d.getCanvas().forceFocus();
                        break;
                    }
                    case SELECT_OPTION_WITH_SAME_COLOURS:
                        win.getMntmWithSameColour().setSelection(!win.getMntmWithSameColour().getSelection());
                        win.loadSelectorSettings();
                        break;
                    case SELECT_CONNECTED:
                    {
                        final SelectorSettings sels = win.loadSelectorSettings();
                        sels.setScope(SelectorSettings.CONNECTED);
                        vm.selector(sels);
                        vm.syncWithTextEditors(true);
                        break;
                    }
                    case SELECT_TOUCHING:
                    {
                        final SelectorSettings sels = win.loadSelectorSettings();
                        sels.setScope(SelectorSettings.TOUCHING);
                        vm.selector(sels);
                        vm.syncWithTextEditors(true);
                        break;
                    }
                    case FLIP_ROTATE_VERTICES:
                        vm.flipSelection();
                        break;
                    case MERGE_TO_AVERAGE:
                        vm.merge(MergeTo.AVERAGE, true, false);
                        break;
                    case MERGE_TO_LAST:
                        vm.merge(MergeTo.LAST_SELECTED, true, false);
                        break;
                    case SPLIT:
                        vm.split(2);
                        break;
                    case MODE_X:
                        win.setWorkingLayer(ManipulatorAxisMode.X);
                        break;
                    case MODE_Y:
                        win.setWorkingLayer(ManipulatorAxisMode.Y);
                        break;
                    case MODE_Z:
                        win.setWorkingLayer(ManipulatorAxisMode.Z);
                        break;
                    case MODE_XY:
                        win.setWorkingLayer(ManipulatorAxisMode.XY);
                        break;
                    case MODE_XZ:
                        win.setWorkingLayer(ManipulatorAxisMode.XZ);
                        break;
                    case MODE_YZ:
                        win.setWorkingLayer(ManipulatorAxisMode.YZ);
                        break;
                    case MODE_XYZ:
                        win.setWorkingLayer(ManipulatorAxisMode.XYZ);
                        break;
                    case TRANSFORM_UP:
                    case TRANSFORM_UP_COPY:
                    case TRANSFORM_RIGHT:
                    case TRANSFORM_RIGHT_COPY:
                        if (win.getWorkingLayer() == ManipulatorAxisMode.NONE || win.getWorkingAction() == WorkingMode.SELECT) break;
                        if (ctrlPressed) {
                            win.setMovingAdjacentData(false);
                            GuiStatusManager.updateStatus();
                            vm.copy();
                            vm.paste(null);
                        }
                        c3d.getManipulator().smallIncrement(win.getWorkingAction(), win.getWorkingLayer(), win.getTransformationMode(), c3d);
                        c3d.getManipulator().applyTranslation(c3d);
                        c3d.getMouse().checkSyncEditMode(vm, df);
                        c3d.getManipulator().resetTranslation();
                        c3d.getMouse().syncManipulator();
                        break;
                    case TRANSFORM_DOWN:
                    case TRANSFORM_DOWN_COPY:
                    case TRANSFORM_LEFT:
                    case TRANSFORM_LEFT_COPY:
                        if (win.getWorkingLayer() == ManipulatorAxisMode.NONE || win.getWorkingAction() == WorkingMode.SELECT) break;
                        if (ctrlPressed) {
                            win.setMovingAdjacentData(false);
                            GuiStatusManager.updateStatus();
                            vm.copy();
                            vm.paste(null);
                        }
                        c3d.getManipulator().smallDecrement(win.getWorkingAction(), win.getWorkingLayer(), win.getTransformationMode(), c3d);
                        c3d.getManipulator().applyTranslation(c3d);
                        c3d.getMouse().checkSyncEditMode(vm, df);
                        c3d.getManipulator().resetTranslation();
                        c3d.getMouse().syncManipulator();
                        break;
                    case TRANSLATE_UP:
                        translateView(c3d, 0, -100 /c3d.getViewportPixelPerLDU());
                        break;
                    case TRANSLATE_RIGHT:
                        translateView(c3d, -100 / c3d.getViewportPixelPerLDU(), 0);
                        break;
                    case TRANSLATE_DOWN:
                        translateView(c3d, 0, 100 / c3d.getViewportPixelPerLDU());
                        break;
                    case TRANSLATE_LEFT:
                        translateView(c3d, 100 / c3d.getViewportPixelPerLDU(), 0);
                        break;
                    case CLOSE_VIEW:
                        c3d.getModifier().closeView();
                        break;
                    }
                }
            } else if (keyEventType == SWT.KeyUp) {
                NLogger.debug(KeyStateManager.class, "[Key ({0}) up]", keyCode); //$NON-NLS-1$
                pressedKeyCodes.remove(keyCode);
                setKeyState(keyCode, false);
                final boolean ctrlPressed = (event.stateMask & SWT.CTRL) != 0;
                final boolean altPressed = (event.stateMask & SWT.ALT) != 0;
                final boolean shiftPressed = (event.stateMask & SWT.SHIFT) != 0;
                final StringBuilder sb = new StringBuilder();
                sb.append(keyCode);
                sb.append(ctrlPressed ? "+Ctrl" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(altPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(shiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
                final String key = sb.toString();
                final Task t = taskMap.get(key);
                if (t != null) {
                    final DatFile df = c3d.getLockableDatFileReference();
                    final VertexManager vm = df.getVertexManager();
                    switch (t) {
                    case LMB:
                        vm.addSnapshot();
                        {
                            Composite3D lc3d = DatFile.getLastHoveredComposite();
                            if (lc3d == null) lc3d = c3d;
                            lc3d.getCanvas().forceFocus();
                            Event mouseEvent = new Event();
                            mouseEvent.type = SWT.MouseUp;
                            mouseEvent.button = MouseButton.LEFT;
                            Vector2f mpos = lc3d.getMousePosition();
                            mouseEvent.x = (int) mpos.x;
                            mouseEvent.y = (int) mpos.y;
                            lc3d.getMouse().mouseUp(mouseEvent);
                            break;
                        }
                    case RMB:
                        vm.addSnapshot();
                        {
                            Composite3D lc3d = DatFile.getLastHoveredComposite();
                            if (lc3d == null) lc3d = c3d;
                            lc3d.getCanvas().forceFocus();
                            Event mouseEvent = new Event();
                            mouseEvent.type = SWT.MouseUp;
                            mouseEvent.button = MouseButton.RIGHT;
                            Vector2f mpos = lc3d.getMousePosition();
                            mouseEvent.x = (int) mpos.x;
                            mouseEvent.y = (int) mpos.y;
                            lc3d.getMouse().mouseUp(mouseEvent);
                            break;
                        }
                    case MMB:
                        vm.addSnapshot();
                        {
                            Composite3D lc3d = DatFile.getLastHoveredComposite();
                            if (lc3d == null) lc3d = c3d;
                            lc3d.getCanvas().forceFocus();
                            Event mouseEvent = new Event();
                            mouseEvent.type = SWT.MouseUp;
                            mouseEvent.button = MouseButton.MIDDLE;
                            Vector2f mpos = lc3d.getMousePosition();
                            mouseEvent.x = (int) mpos.x;
                            mouseEvent.y = (int) mpos.y;
                            lc3d.getMouse().mouseUp(mouseEvent);
                            lc3d.getCanvas().getDisplay().post(mouseEvent);
                            break;
                        }
                    default:
                        break;
                    }
                }
            }

            // Synchronise key state with other composites
            ArrayList<OpenGLRenderer> r = Editor3DWindow.getRenders();
            for (OpenGLRenderer renderer : r) {
                renderer.getC3D().getKeys().synchronise(this);
            }
        } else {
            // Logic for CompositePrimitive
            if (keyEventType == SWT.KeyDown && !pressedKeyCodes.contains(keyCode)) {
                NLogger.debug(KeyStateManager.class, "[Key ({0}) down]", keyCode); //$NON-NLS-1$
                final boolean ctrlPressed = (event.stateMask & SWT.CTRL) != 0;
                final boolean altPressed = (event.stateMask & SWT.ALT) != 0;
                final boolean shiftPressed = (event.stateMask & SWT.SHIFT) != 0;
                final boolean cmdPressed = (event.stateMask & SWT.COMMAND) != 0;
                final StringBuilder sb = new StringBuilder();
                sb.append(keyCode);
                sb.append(ctrlPressed ? "+Ctrl" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(altPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(shiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(cmdPressed ? "+Cmd" : ""); //$NON-NLS-1$//$NON-NLS-2$
                final String key = sb.toString();
                final Task t = taskMap.get(key);
                if (t != null) {
                    switch (t) {
                    case LMB:
                    {
                        cp.getCanvas().forceFocus();
                        Event mouseEvent = new Event();
                        mouseEvent.type = SWT.MouseDown;
                        mouseEvent.button = MouseButton.LEFT;
                        Vector2f mpos = cp.getMousePosition();
                        mouseEvent.x = (int) mpos.x;
                        mouseEvent.y = (int) mpos.y;
                        cp.mouseDown(mouseEvent);
                        cp.getCanvas().getDisplay().post(mouseEvent);
                        break;
                    }
                    case RMB:
                    {
                        if (!Cocoa.isCocoa) {
                            cp.getCanvas().forceFocus();
                            Event mouseEvent = new Event();
                            mouseEvent.type = SWT.MouseDown;
                            mouseEvent.button = MouseButton.RIGHT;
                            Vector2f mpos = cp.getMousePosition();
                            mouseEvent.x = (int) mpos.x;
                            mouseEvent.y = (int) mpos.y;
                            cp.mouseDown(mouseEvent);
                            cp.getCanvas().getDisplay().post(mouseEvent);
                        }
                        break;
                    }
                    case MMB:
                    {
                        cp.getCanvas().forceFocus();
                        Event mouseEvent = new Event();
                        mouseEvent.type = SWT.MouseDown;
                        mouseEvent.button = MouseButton.MIDDLE;
                        Vector2f mpos = cp.getMousePosition();
                        mouseEvent.x = (int) mpos.x;
                        mouseEvent.y = (int) mpos.y;
                        cp.mouseDown(mouseEvent);
                        cp.getCanvas().getDisplay().post(mouseEvent);
                        break;
                    }
                    default:
                        break;
                    }
                }
                if (keyCode == SWT.PAGE_UP || keyCode == SWT.ARROW_UP) {
                    cp.scroll(false);
                } else if (keyCode == SWT.PAGE_DOWN || keyCode == SWT.ARROW_DOWN) {
                    cp.scroll(true);
                } else {
                    pressedKeyCodes.add(keyCode);
                }
                setKeyState(keyCode, true);
            } else if (keyEventType == SWT.KeyUp) {
                NLogger.debug(KeyStateManager.class, "[Key ({0}) up]", keyCode); //$NON-NLS-1$
                pressedKeyCodes.remove(keyCode);
                setKeyState(keyCode, false);
                final boolean ctrlPressed = (event.stateMask & SWT.CTRL) != 0;
                final boolean altPressed = (event.stateMask & SWT.ALT) != 0;
                final boolean shiftPressed = (event.stateMask & SWT.SHIFT) != 0;
                final StringBuilder sb = new StringBuilder();
                sb.append(keyCode);
                sb.append(ctrlPressed ? "+Ctrl" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(altPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(shiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
                final String key = sb.toString();
                final Task t = taskMap.get(key);
                if (t != null) {
                    switch (t) {
                    case LMB:
                    {
                        cp.getCanvas().forceFocus();
                        Event mouseEvent = new Event();
                        mouseEvent.type = SWT.MouseUp;
                        mouseEvent.button = MouseButton.LEFT;
                        Vector2f mpos = cp.getMousePosition();
                        mouseEvent.x = (int) mpos.x;
                        mouseEvent.y = (int) mpos.y;
                        cp.mouseUp(mouseEvent);
                        cp.getCanvas().getDisplay().post(mouseEvent);
                        break;
                    }
                    case RMB:
                    {
                        if (!Cocoa.isCocoa) {
                            cp.getCanvas().forceFocus();
                            Event mouseEvent = new Event();
                            mouseEvent.type = SWT.MouseUp;
                            mouseEvent.button = MouseButton.RIGHT;
                            Vector2f mpos = cp.getMousePosition();
                            mouseEvent.x = (int) mpos.x;
                            mouseEvent.y = (int) mpos.y;
                            cp.mouseUp(mouseEvent);
                            cp.getCanvas().getDisplay().post(mouseEvent);
                        }
                        break;
                    }
                    case MMB:
                    {
                        cp.getCanvas().forceFocus();
                        Event mouseEvent = new Event();
                        mouseEvent.type = SWT.MouseUp;
                        mouseEvent.button = MouseButton.MIDDLE;
                        Vector2f mpos = cp.getMousePosition();
                        mouseEvent.x = (int) mpos.x;
                        mouseEvent.y = (int) mpos.y;
                        cp.mouseUp(mouseEvent);
                        cp.getCanvas().getDisplay().post(mouseEvent);
                        break;
                    }
                    default:
                        break;
                    }
                }
            }
        }
    }

    /**
     * Sets the state of released and pressed keys
     *
     * @param keyCode
     *            the code to evaluate
     * @param isPressed
     *            {@code true} on {@code SWT.KeyDown} events
     */
    public void setKeyState(int keyCode, boolean isPressed) {
        switch (keyCode) {
        case SWT.ALT:
            altPressed = isPressed;
            break;
        case SWT.SHIFT:
            shiftPressed = isPressed;
            break;
        case SWT.CTRL:
            ctrlPressed = isPressed;
            break;
        case SWT.COMMAND:
            cmdPressed = isPressed;
            break;
        }
    }

    public void synchronise(KeyStateManager ksm) {
        this.altPressed = ksm.altPressed;
        this.shiftPressed = ksm.shiftPressed;
        this.ctrlPressed = ksm.ctrlPressed;
        this.cmdPressed = ksm.cmdPressed;
    }

    public static HashSet<String> getReservedKeyCodes() {
        return reservedKeyCodes;
    }

    public static HashMap<String, Task> getTaskmap() {
        return taskMap;
    }

    public static HashMap<String, TextTask> getTextTaskmap() {
        return textTaskMap;
    }

    public static HashMap<Task, String> getTaskKeymap() {
        return taskKeyMap;
    }

    public static HashMap<TextTask, String> getTextTaskKeymap() {
        return textTaskKeyMap;
    }

    public static void addTask(Task t, int keyCode) {
        addTask(t, SWT.NONE, keyCode);
    }

    public static void addTask(TextTask t, int keyCode) {
        addTask(t, SWT.NONE, keyCode);
    }

    public static void addTask(Task t, int stateMask, int keyCode) {
        String[] s = getStrings(keyCode, stateMask);
        taskMap.put(s[0], t);
        taskKeyMap.put(t, s[1]);
    }

    public static void addTask(TextTask t, int stateMask, int keyCode) {
        String[] s = getStrings(keyCode, stateMask);
        textTaskMap.put(s[0], t);
        textTaskKeyMap.put(t, s[1]);
    }

    private static String[] getStrings(int keyCode, int stateMask) {
        final boolean ctrlPressed = (stateMask & SWT.CTRL) != 0;
        final boolean altPressed = (stateMask & SWT.ALT) != 0;
        final boolean shiftPressed = (stateMask & SWT.SHIFT) != 0;
        final boolean cmdPressed = (stateMask & SWT.COMMAND) != 0;
        String[] s = new String[2];
        final StringBuilder sb = new StringBuilder();
        sb.append(keyCode);
        sb.append(ctrlPressed ? "+Ctrl" : ""); //$NON-NLS-1$//$NON-NLS-2$
        sb.append(altPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
        sb.append(shiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
        sb.append(cmdPressed ? "+Cmd" : ""); //$NON-NLS-1$//$NON-NLS-2$
        s[0] = sb.toString();
        Event event = new Event();
        event.keyCode = keyCode;
        if (ctrlPressed) event.stateMask = event.stateMask | SWT.CTRL;
        if (altPressed) event.stateMask = event.stateMask | SWT.ALT;
        if (shiftPressed) event.stateMask = event.stateMask | SWT.SHIFT;
        if (cmdPressed) event.stateMask = event.stateMask | SWT.COMMAND;
        s[1] = KeyBoardHelper.getKeyString(event);
        return s;
    }

    public static void addTooltipText(final NButton btn, final String text, final TextTask t) {
        btn.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                btn.setToolTipText(text + " (" + textTaskKeyMap.get(t) +")"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });
    }

    public static void addTooltipText(final NButton btn, final String text, final Task t) {
        btn.addMouseMoveListener(new MouseMoveListener() {
            @Override
            public void mouseMove(MouseEvent e) {
                btn.setToolTipText(text + " (" + taskKeyMap.get(t) +")"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        });
    }

    public static void addKeyText(final MenuItem mntm, final String text, final Task t) {
        mntm.setText(text + "\t" + taskKeyMap.get(t)); //$NON-NLS-1$
        mntm.addArmListener(new ArmListener() {
            @Override
            public void widgetArmed(ArmEvent e) {
                mntm.setText(text + "\t" + taskKeyMap.get(t)); //$NON-NLS-1$
            }
        });
    }

    public static void addKeyText(final MenuItem mntm, final String text, final TextTask t) {
        mntm.setText(text + "\t" + textTaskKeyMap.get(t)); //$NON-NLS-1$
        mntm.addArmListener(new ArmListener() {
            @Override
            public void widgetArmed(ArmEvent e) {
                mntm.setText(text + "\t" + textTaskKeyMap.get(t)); //$NON-NLS-1$
            }
        });
    }

    public static void addKeyText(final MenuItem mntm, final String text) {
        mntm.setText(text);
    }

    public static void addTooltipText(NButton btn, final String text) {
        btn.setToolTipText(text);
    }

    public static String getMapKey(Task t1) {
        for (String k : taskMap.keySet()) {
            if (taskMap.get(k) == t1) {
                return k;
            }
        }
        return null;
    }

    public static String getMapKey(TextTask t2) {
        for (String k : textTaskMap.keySet()) {
            if (textTaskMap.get(k) == t2) {
                return k;
            }
        }
        return null;
    }

    public static void changeKey(String oldKey, String newKey, String keyString, Task t) {
        taskMap.remove(oldKey);
        taskMap.put(newKey, t);
        taskKeyMap.put(t, keyString);
    }

    public static void changeKey(String oldKey, String newKey, String keyString, TextTask t) {
        textTaskMap.remove(oldKey);
        textTaskMap.put(newKey, t);
        textTaskKeyMap.put(t, keyString);
    }

    public static boolean hasTextTaskKey(String key) {
        if (key != null) {
            for (String k : textTaskMap.keySet()) {
                if (key.equals(k)) return true;
            }
        }
        return false;
    }

    public static boolean hasTaskKey(String key) {
        if (key != null) {
            for (String k : taskMap.keySet()) {
                if (key.equals(k)) return true;
            }
        }
        return false;
    }

    private static void translateView(Composite3D c3d, float dx, float dy) {
        PerspectiveCalculator perspective = c3d.getPerspectiveCalculator();
        Matrix4f viewport_rotation = c3d.getRotation();
        Matrix4f viewport_translation = c3d.getTranslation();
        Matrix4f old_viewport_translation = new Matrix4f();
        Matrix4f.load(c3d.getTranslation(), old_viewport_translation);
        Vector4f xAxis4f_translation = new Vector4f(dx, 0, 0, 1.0f);
        Vector4f yAxis4f_translation = new Vector4f(0, dy, 0, 1.0f);
        Matrix4f ovr_inverse2 = Matrix4f.invert(viewport_rotation, null);
        Matrix4f.transform(ovr_inverse2, xAxis4f_translation, xAxis4f_translation);
        Matrix4f.transform(ovr_inverse2, yAxis4f_translation, yAxis4f_translation);
        Vector3f xAxis3 = new Vector3f(xAxis4f_translation.x, xAxis4f_translation.y, xAxis4f_translation.z);
        Vector3f yAxis3 = new Vector3f(yAxis4f_translation.x, yAxis4f_translation.y, yAxis4f_translation.z);
        Matrix4f.load(old_viewport_translation, viewport_translation);
        Matrix4f.translate(xAxis3, old_viewport_translation, viewport_translation);
        Matrix4f.translate(yAxis3, viewport_translation, viewport_translation);
        perspective.calculateOriginData();
        c3d.getVertexManager().getResetTimer().set(true);
        if (c3d.isSyncTranslation()) {
            float tx = c3d.getTranslation().m30;
            float ty = c3d.getTranslation().m31;
            float tz = c3d.getTranslation().m32;
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d2 = renderer.getC3D();
                if (!c3d2.isDisposed() && c3d != c3d2 && c3d.getLockableDatFileReference().equals(c3d2.getLockableDatFileReference())) {
                    c3d2.getTranslation().m30 = tx;
                    c3d2.getTranslation().m31 = ty;
                    c3d2.getTranslation().m32 = tz;
                    ((ScalableComposite) c3d2.getParent()).redrawScales();
                    c3d2.getPerspectiveCalculator().initializeViewportPerspective();
                }
            }
        }
    }
}
