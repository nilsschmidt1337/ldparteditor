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
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.ScalableComposite;
import org.nschmidt.ldparteditor.composite.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.ManipulatorAxisMode;
import org.nschmidt.ldparteditor.enumtype.ManipulatorScope;
import org.nschmidt.ldparteditor.enumtype.MergeTo;
import org.nschmidt.ldparteditor.enumtype.MouseButton;
import org.nschmidt.ldparteditor.enumtype.Perspective;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.enumtype.TextTask;
import org.nschmidt.ldparteditor.enumtype.WorkingMode;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.KeyBoardHelper;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.helper.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.helper.composite3d.MouseActions;
import org.nschmidt.ldparteditor.helper.composite3d.PerspectiveCalculator;
import org.nschmidt.ldparteditor.helper.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helper.composite3d.SelectorSettings;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.AddToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.ColourToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.InsertAtCursorPositionToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.ManipulatorScopeToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.ManipulatorToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToggleToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.NewOpenSaveDatfileToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.NewOpenSaveProjectToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.TransformationModeToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.WorkingTypeToolItem;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Stores information about keys pressed on the keyboard and triggers keyboard
 * events.
 *
 */
public class KeyStateManager {

    private static final String CMD_STRING = "+Cmd"; //$NON-NLS-1$

    private static final String CTRL_STRING = "+Ctrl"; //$NON-NLS-1$

    /** The 3D Composite */
    private final Composite3D c3d;

    private final CompositePrimitive cp;

    /** A set of all keys which are pressed at the moment */
    private Set<Integer> pressedKeyCodes = new HashSet<>();
    /** A map which assigns a pressed key to a task */
    private static final Map<String, Task> taskMap = new HashMap<>();
    private static final Map<Task, String> taskKeyMap = new EnumMap<>(Task.class);
    /** A set of all reserved keys */
    private static final Set<String> reservedKeyCodes = new HashSet<>();
    /** A map which assigns a pressed key to a task (for the text editor)*/
    private static final Map<String, TextTask> textTaskMap = new HashMap<>();
    private static final Map<TextTask, String> textTaskKeyMap = new EnumMap<>(TextTask.class);

    private static final Map<Task, String> backupTaskKeyMap = new EnumMap<>(Task.class);
    private static final Map<TextTask, String> backupTextTaskKeyMap = new EnumMap<>(TextTask.class);
    private static final Map<String, Task> backupTaskMap = new HashMap<>();
    private static final Map<String, TextTask> backupTextTaskMap = new HashMap<>();

    private int multi = 100;
    private int colourNumber = 0;

    public static int tmpStateMask = 0;
    public static int tmpKeyCode = 0;
    public static String tmpKeyString = ""; //$NON-NLS-1$
    public static String tmpMapKey = ""; //$NON-NLS-1$

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

        if (Cocoa.IS_COCOA) {

            reservedKeyCodes.add(SWT.ARROW_UP + CMD_STRING);
            reservedKeyCodes.add(SWT.ARROW_RIGHT + CMD_STRING);
            reservedKeyCodes.add(SWT.ARROW_DOWN + CMD_STRING);
            reservedKeyCodes.add(SWT.ARROW_LEFT + CMD_STRING);

            reservedKeyCodes.add(SWT.BS + CMD_STRING);
            reservedKeyCodes.add((int) 'x' + CMD_STRING);
            reservedKeyCodes.add((int) 'c' + CMD_STRING);
            reservedKeyCodes.add((int) 'v' + CMD_STRING);

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
            addTask(Task.SHOW_GRID_3D, SWT.COMMAND | SWT.SHIFT, 'g');
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
            addTask(Task.MERGE_TO_NEAREST_VERTEX, SWT.COMMAND, 'n');
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
            addTask(TextTask.EDITORTEXT_INSERT_REFERENCE_MIRRORED_ON_X, SWT.COMMAND | SWT.ALT, 'r');

            addTask(TextTask.EDITORTEXT_LINE_UP, SWT.ALT, SWT.ARROW_UP);
            addTask(TextTask.EDITORTEXT_LINE_DOWN, SWT.ALT, SWT.ARROW_DOWN);

            addTask(TextTask.EDITORTEXT_SIZE_DECREASE, SWT.COMMAND, '-');
            addTask(TextTask.EDITORTEXT_SIZE_INCREASE, SWT.COMMAND, '+');

            addTask(TextTask.EDITORTEXT_FOLLOW_LINK, SWT.COMMAND, SWT.SHIFT);

            addTask(Task.CLOSE_VIEW, 'q');

            addTask(Task.PERSPECTIVE_TOP, SWT.COMMAND, SWT.KEYPAD_5);
            addTask(Task.PERSPECTIVE_BACK, SWT.COMMAND, SWT.KEYPAD_8);
            addTask(Task.PERSPECTIVE_FRONT, SWT.COMMAND, SWT.KEYPAD_2);
            addTask(Task.PERSPECTIVE_LEFT, SWT.COMMAND, SWT.KEYPAD_4);
            addTask(Task.PERSPECTIVE_RIGHT, SWT.COMMAND, SWT.KEYPAD_6);
            addTask(Task.PERSPECTIVE_BOTTOM, SWT.COMMAND, SWT.KEYPAD_0);

            addTask(Task.PERSPECTIVE_TWO_THIRDS, SWT.COMMAND, SWT.KEYPAD_3);

            addTask(Task.RENDERMODE_NO_BACKFACE_CULLING, SWT.ALT, SWT.KEYPAD_1);
            addTask(Task.RENDERMODE_RANDOM_COLOURS, SWT.ALT, SWT.KEYPAD_2);
            addTask(Task.RENDERMODE_GREEN_FRONTFACES_RED_BACKFACES, SWT.ALT, SWT.KEYPAD_3);
            addTask(Task.RENDERMODE_RED_BACKFACES, SWT.ALT, SWT.KEYPAD_4);
            addTask(Task.RENDERMODE_REAL_BACKFACE_CULLING, SWT.ALT, SWT.KEYPAD_5);
            addTask(Task.RENDERMODE_LDRAW_STANDARD, SWT.ALT, SWT.KEYPAD_6);
            addTask(Task.RENDERMODE_SPECIAL_CONDLINE, SWT.ALT, SWT.KEYPAD_7);
            addTask(Task.RENDERMODE_COPLANARITY_HEATMAP, SWT.ALT, SWT.KEYPAD_8);
            addTask(Task.RENDERMODE_WIREFRAME, SWT.ALT, SWT.KEYPAD_9);

            addTask(Task.RESET_MANIPULATOR, SWT.SHIFT | 'r');

            addTask(Task.CONDLINE_TO_LINE, SWT.SHIFT, 'l');
            addTask(Task.LINE_TO_CONDLINE, SWT.SHIFT, 'c');

            addTask(Task.QUICK_MOVE, 'g');
            addTask(Task.QUICK_SCALE, 's');
            addTask(Task.QUICK_ROTATE, 'r');

            addTask(Task.QUICK_LOCK_X, 'x');
            addTask(Task.QUICK_LOCK_Y, 'y');
            addTask(Task.QUICK_LOCK_Z, 'z');

            addTask(Task.QUICK_LOCK_YZ, SWT.CTRL | SWT.SHIFT, 'x');
            addTask(Task.QUICK_LOCK_XZ, SWT.CTRL | SWT.SHIFT, 'y');
            addTask(Task.QUICK_LOCK_XY, SWT.CTRL | SWT.SHIFT, 'z');

            addTask(Task.TRIANGLE_TO_QUAD, SWT.SHIFT, 'q');
            addTask(Task.QUAD_TO_TRIANGLE, SWT.SHIFT, 't');

            addTask(Task.EDGER2, SWT.SHIFT, 'e');
        } else {

            reservedKeyCodes.add(SWT.ARROW_UP + CTRL_STRING);
            reservedKeyCodes.add(SWT.ARROW_RIGHT + CTRL_STRING);
            reservedKeyCodes.add(SWT.ARROW_DOWN + CTRL_STRING);
            reservedKeyCodes.add(SWT.ARROW_LEFT + CTRL_STRING);

            reservedKeyCodes.add(SWT.DEL + ""); //$NON-NLS-1$
            reservedKeyCodes.add((int) 'x' + CTRL_STRING);
            reservedKeyCodes.add((int) 'c' + CTRL_STRING);
            reservedKeyCodes.add((int) 'v' + CTRL_STRING);

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
            addTask(Task.SHOW_GRID_3D, SWT.CTRL | SWT.SHIFT, 'g');
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
            addTask(Task.MERGE_TO_NEAREST_VERTEX, SWT.CTRL, 'n');
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
            addTask(TextTask.EDITORTEXT_INSERT_REFERENCE_MIRRORED_ON_X, SWT.CTRL | SWT.SHIFT, 'r');

            addTask(TextTask.EDITORTEXT_LINE_UP, SWT.ALT, SWT.ARROW_UP);
            addTask(TextTask.EDITORTEXT_LINE_DOWN, SWT.ALT, SWT.ARROW_DOWN);

            addTask(TextTask.EDITORTEXT_SIZE_DECREASE, SWT.CTRL, '-');
            addTask(TextTask.EDITORTEXT_SIZE_INCREASE, SWT.CTRL, '+');

            addTask(TextTask.EDITORTEXT_FOLLOW_LINK, SWT.CTRL, SWT.SHIFT);

            addTask(Task.CLOSE_VIEW, 'q');

            addTask(Task.PERSPECTIVE_TOP, SWT.CTRL, SWT.KEYPAD_5);
            addTask(Task.PERSPECTIVE_BACK, SWT.CTRL, SWT.KEYPAD_8);
            addTask(Task.PERSPECTIVE_FRONT, SWT.CTRL, SWT.KEYPAD_2);
            addTask(Task.PERSPECTIVE_LEFT, SWT.CTRL, SWT.KEYPAD_4);
            addTask(Task.PERSPECTIVE_RIGHT, SWT.CTRL, SWT.KEYPAD_6);
            addTask(Task.PERSPECTIVE_BOTTOM, SWT.CTRL, SWT.KEYPAD_0);

            addTask(Task.PERSPECTIVE_TWO_THIRDS, SWT.CTRL, SWT.KEYPAD_3);

            addTask(Task.RENDERMODE_NO_BACKFACE_CULLING, SWT.ALT, SWT.KEYPAD_1);
            addTask(Task.RENDERMODE_RANDOM_COLOURS, SWT.ALT, SWT.KEYPAD_2);
            addTask(Task.RENDERMODE_GREEN_FRONTFACES_RED_BACKFACES, SWT.ALT, SWT.KEYPAD_3);
            addTask(Task.RENDERMODE_RED_BACKFACES, SWT.ALT, SWT.KEYPAD_4);
            addTask(Task.RENDERMODE_REAL_BACKFACE_CULLING, SWT.ALT, SWT.KEYPAD_5);
            addTask(Task.RENDERMODE_LDRAW_STANDARD, SWT.ALT, SWT.KEYPAD_6);
            addTask(Task.RENDERMODE_SPECIAL_CONDLINE, SWT.ALT, SWT.KEYPAD_7);
            addTask(Task.RENDERMODE_COPLANARITY_HEATMAP, SWT.ALT, SWT.KEYPAD_8);
            addTask(Task.RENDERMODE_WIREFRAME, SWT.ALT, SWT.KEYPAD_9);

            addTask(Task.RESET_MANIPULATOR, SWT.SHIFT | 'r');

            addTask(Task.CONDLINE_TO_LINE, SWT.SHIFT, 'l');
            addTask(Task.LINE_TO_CONDLINE, SWT.SHIFT, 'c');

            addTask(Task.QUICK_MOVE, 'g');
            addTask(Task.QUICK_SCALE, 's');
            addTask(Task.QUICK_ROTATE, 'r');

            addTask(Task.QUICK_LOCK_X, 'x');
            addTask(Task.QUICK_LOCK_Y, 'y');
            addTask(Task.QUICK_LOCK_Z, 'z');

            addTask(Task.QUICK_LOCK_YZ, SWT.CTRL | SWT.SHIFT, 'x');
            addTask(Task.QUICK_LOCK_XZ, SWT.CTRL | SWT.SHIFT, 'y');
            addTask(Task.QUICK_LOCK_XY, SWT.CTRL | SWT.SHIFT, 'z');

            addTask(Task.TRIANGLE_TO_QUAD, SWT.SHIFT, 'q');
            addTask(Task.QUAD_TO_TRIANGLE, SWT.SHIFT, 't');

            addTask(Task.EDGER2, SWT.SHIFT, 'e');
        }

        backupTaskMap.putAll(taskMap);
        backupTaskKeyMap.putAll(taskKeyMap);
        backupTextTaskMap.putAll(textTaskMap);
        backupTextTaskKeyMap.putAll(textTaskKeyMap);
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
                final boolean tmpCtrlPressed = (event.stateMask & SWT.CTRL) != 0;
                final boolean tmpAltPressed = (event.stateMask & SWT.ALT) != 0;
                final boolean tmpShiftPressed = (event.stateMask & SWT.SHIFT) != 0;
                final boolean tmpCmdPressed = (event.stateMask & SWT.COMMAND) != 0;
                final StringBuilder sb = new StringBuilder();
                sb.append(keyCode);
                sb.append(tmpCtrlPressed ? CTRL_STRING : ""); //$NON-NLS-1$
                sb.append(tmpAltPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(tmpShiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(tmpCmdPressed ? CMD_STRING : ""); //$NON-NLS-1$
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
                        vm.delete(MiscToggleToolItem.isMovingAdjacentData(), true);
                        break;
                    case INSERT_AT_CURSOR:
                        InsertAtCursorPositionToolItem.toggleInsertAtCursor();
                        break;
                    case ESC:
                        multi = 100;
                        colourNumber = 0;
                        if (df.getObjVertex1() == null && df.getObjVertex2() == null && df.getObjVertex3() == null && df.getObjVertex4() == null) {
                            AddToolItem.disableAddAction();
                        }
                        MiscToolItem.activateAllTypes();
                        df.setObjVertex1(null);
                        df.setObjVertex2(null);
                        df.setObjVertex3(null);
                        df.setObjVertex4(null);
                        df.setNearestObjVertex1(null);
                        df.setNearestObjVertex2(null);
                        win.setWorkingLayer(ManipulatorAxisMode.NONE);
                        win.updateInitialScale(BigDecimal.ZERO, BigDecimal.ONE, true);
                        if (!c3d.isQuicklyTransforming()) {
                            vm.clearSelection();
                            final Manipulator mani = c3d.getManipulator();
                            if (mani.isModified()) {
                                mani.restoreBackup();
                                mani.doBackup();
                            }
                        } else {
                            c3d.getManipulator().restoreBackup();
                        }

                        vm.resetSlantingMatrixProjector();
                        c3d.setQuicklyTransforming(false);
                        break;
                    case COPY:
                        vm.copy();
                        break;
                    case CUT:
                        vm.copy();
                        vm.delete(MiscToggleToolItem.isMovingAdjacentData(), true);
                        break;
                    case PASTE:
                        vm.paste(MiscToolItem.loadSelectorSettings());
                        if (WorkbenchManager.getUserSettingState().isDisableMAD3D()) {
                            MiscToggleToolItem.setMovingAdjacentData(false);
                            GuiStatusManager.updateStatus();
                        }
                        break;
                    case MODE_COMBINED:
                        modeCombined(df, vm);
                        break;
                    case MODE_MOVE:
                        modeMove(df, vm);
                        break;
                    case MODE_ROTATE:
                        modeRotate(df, vm);
                        break;
                    case MODE_SCALE:
                        modeScale(df, vm);
                        break;
                    case MODE_SELECT:
                        modeSelect(df, vm);
                        break;
                    case MOVE_TO_AVG:
                        ManipulatorToolItem.mntmManipulatorToAverage();
                        break;
                    case MOVE_ADJACENT_DATA:
                        MiscToggleToolItem.toggleMoveAdjacentData();
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
                            if (LDConfig.hasColour(colourNumber)) {
                                ColourToolItem.setLastUsedColour2(LDConfig.getColour(colourNumber));
                            }
                            colourNumber = 0;
                        }
                        break;
                    case ADD_COMMENTS:
                        AddToolItem.setAddState(0);
                        pressedKeyCodes.remove(keyCode);
                        break;
                    case ADD_DISTANCE:
                        AddToolItem.setAddState(6);
                        break;
                    case ADD_PROTRACTOR:
                        AddToolItem.setAddState(7);
                        break;
                    case ADD_CONDLINE:
                        AddToolItem.setAddState(5);
                        break;
                    case ADD_LINE:
                        AddToolItem.setAddState(2);
                        break;
                    case ADD_QUAD:
                        AddToolItem.setAddState(4);
                        break;
                    case ADD_TRIANGLE:
                        AddToolItem.setAddState(3);
                        break;
                    case ADD_VERTEX:
                        AddToolItem.setAddState(1);
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
                    case RESET_MANIPULATOR:
                        c3d.getManipulator().reset();
                        break;
                    case OBJ_FACE:
                        WorkingTypeToolItem.setObjMode(1);
                        break;
                    case OBJ_LINE:
                        WorkingTypeToolItem.setObjMode(2);
                        break;
                    case OBJ_PRIMITIVE:
                        WorkingTypeToolItem.setObjMode(3);
                        break;
                    case OBJ_VERTEX:
                        WorkingTypeToolItem.setObjMode(0);
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
                        if (!Cocoa.IS_COCOA) {
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
                        break;
                    }
                    case REDO:
                        df.redo(false);
                        pressedKeyCodes.remove(keyCode);
                        break;
                    case UNDO:
                        df.undo(false);
                        pressedKeyCodes.remove(keyCode);
                        break;
                    case SAVE:
                        if (!df.isReadOnly()) {
                            if (df.isVirtual()) {
                                if (NewOpenSaveDatfileToolItem.saveAs(win, df)) {
                                    Project.removeUnsavedFile(df);
                                    Project.removeOpenedFile(df);
                                    if (!win.closeDatfile(df)) {
                                        Project.addOpenedFile(df);
                                    }
                                }

                                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                            } else if (df.save()) {
                                NewOpenSaveProjectToolItem.addRecentFile(df);
                                Project.removeUnsavedFile(df);
                                Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                            } else {
                                MessageBox messageBoxError = new MessageBox(win.getShell(), SWT.ICON_ERROR | SWT.OK);
                                messageBoxError.setText(I18n.DIALOG_ERROR);
                                messageBoxError.setMessage(I18n.DIALOG_CANT_SAVE_FILE);
                                messageBoxError.open();
                            }
                        }
                        pressedKeyCodes.remove(keyCode);
                        break;
                    case SELECT_ALL:
                        vm.selectAll(MiscToolItem.loadSelectorSettings(), true);
                        vm.syncWithTextEditors(true);
                        break;
                    case SELECT_ALL_WITH_SAME_COLOURS:
                        vm.selectAllWithSameColours(MiscToolItem.loadSelectorSettings(), true);
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
                    case SHOW_GRID_3D:
                        if (c3d.getMenu().isDisposed()) {
                            break;
                        }
                        c3d.setGridShown3D(!c3d.isGridShown3D());
                        c3d.getMntmShowGrid3D().setSelection(c3d.isGridShown3D());
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
                        MiscToolItem.getMntmWithSameColour().setSelection(!MiscToolItem.getMntmWithSameColour().getSelection());
                        MiscToolItem.loadSelectorSettings();
                        break;
                    case SELECT_CONNECTED:
                    {
                        final SelectorSettings sels = MiscToolItem.loadSelectorSettings();
                        sels.setScope(SelectorSettings.CONNECTED);
                        vm.selector(sels);
                        vm.syncWithTextEditors(true);
                        break;
                    }
                    case SELECT_TOUCHING:
                    {
                        final SelectorSettings sels = MiscToolItem.loadSelectorSettings();
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
                    case MERGE_TO_NEAREST_VERTEX:
                        vm.merge(MergeTo.NEAREST_VERTEX, true, false);
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
                    case TRANSFORM_UP, TRANSFORM_UP_COPY, TRANSFORM_RIGHT, TRANSFORM_RIGHT_COPY:
                        if (win.getWorkingLayer() == ManipulatorAxisMode.NONE || TransformationModeToolItem.getWorkingAction() == WorkingMode.SELECT) break;
                        if (tmpCtrlPressed || tmpCmdPressed) {
                            MiscToggleToolItem.setMovingAdjacentData(false);
                            GuiStatusManager.updateStatus();
                            vm.copy();
                            vm.paste(null);
                        }
                        c3d.getManipulator().smallIncrement(TransformationModeToolItem.getWorkingAction(), win.getWorkingLayer(), ManipulatorScopeToolItem.getTransformationScope(), c3d);
                        c3d.getManipulator().applyTranslation(c3d);
                        MouseActions.checkSyncEditMode(vm, df);
                        c3d.getManipulator().resetTranslation();
                        c3d.getMouse().syncManipulator();
                        break;
                    case TRANSFORM_DOWN, TRANSFORM_DOWN_COPY, TRANSFORM_LEFT, TRANSFORM_LEFT_COPY:
                        if (win.getWorkingLayer() == ManipulatorAxisMode.NONE || TransformationModeToolItem.getWorkingAction() == WorkingMode.SELECT) break;
                        if (tmpCtrlPressed || tmpCmdPressed) {
                            MiscToggleToolItem.setMovingAdjacentData(false);
                            GuiStatusManager.updateStatus();
                            vm.copy();
                            vm.paste(null);
                        }
                        c3d.getManipulator().smallDecrement(TransformationModeToolItem.getWorkingAction(), win.getWorkingLayer(), ManipulatorScopeToolItem.getTransformationScope(), c3d);
                        c3d.getManipulator().applyTranslation(c3d);
                        MouseActions.checkSyncEditMode(vm, df);
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
                    case PERSPECTIVE_FRONT:
                        c3d.getPerspectiveCalculator().setPerspective(Perspective.FRONT);
                        c3d.setPerspectiveOnContextMenu(Perspective.FRONT);
                        break;
                    case PERSPECTIVE_BACK:
                        c3d.getPerspectiveCalculator().setPerspective(Perspective.BACK);
                        c3d.setPerspectiveOnContextMenu(Perspective.BACK);
                        break;
                    case PERSPECTIVE_LEFT:
                        c3d.getPerspectiveCalculator().setPerspective(Perspective.LEFT);
                        c3d.setPerspectiveOnContextMenu(Perspective.LEFT);
                        break;
                    case PERSPECTIVE_RIGHT:
                        c3d.getPerspectiveCalculator().setPerspective(Perspective.RIGHT);
                        c3d.setPerspectiveOnContextMenu(Perspective.RIGHT);
                        break;
                    case PERSPECTIVE_TOP:
                        c3d.getPerspectiveCalculator().setPerspective(Perspective.TOP);
                        c3d.setPerspectiveOnContextMenu(Perspective.TOP);
                        break;
                    case PERSPECTIVE_BOTTOM:
                        c3d.getPerspectiveCalculator().setPerspective(Perspective.BOTTOM);
                        c3d.setPerspectiveOnContextMenu(Perspective.BOTTOM);
                        break;
                    case PERSPECTIVE_TWO_THIRDS:
                        if (c3d.isClassicPerspective()) {
                            c3d.getPerspectiveCalculator().setPerspective(Perspective.TWO_THIRDS);
                            c3d.setPerspectiveOnContextMenu(Perspective.TWO_THIRDS);
                        } else {
                            final Perspective newPerspective = Perspective.toggleTwoThirds(c3d.getPerspectiveIndex());
                            c3d.getPerspectiveCalculator().setPerspective(newPerspective);
                            c3d.setPerspectiveOnContextMenu(newPerspective);
                        }
                        break;
                    case RENDERMODE_NO_BACKFACE_CULLING:
                        c3d.setRenderMode(0);
                        c3d.setRenderModeOnContextMenu(0);
                        break;
                    case RENDERMODE_RANDOM_COLOURS:
                        c3d.setRenderMode(1);
                        c3d.setRenderModeOnContextMenu(1);
                        break;
                    case RENDERMODE_GREEN_FRONTFACES_RED_BACKFACES:
                        c3d.setRenderMode(2);
                        c3d.setRenderModeOnContextMenu(2);
                        break;
                    case RENDERMODE_RED_BACKFACES:
                        c3d.setRenderMode(3);
                        c3d.setRenderModeOnContextMenu(3);
                        break;
                    case RENDERMODE_REAL_BACKFACE_CULLING:
                        c3d.setRenderMode(4);
                        c3d.setRenderModeOnContextMenu(4);
                        break;
                    case RENDERMODE_LDRAW_STANDARD:
                        c3d.setRenderMode(5);
                        c3d.setRenderModeOnContextMenu(5);
                        break;
                    case RENDERMODE_SPECIAL_CONDLINE:
                        c3d.setRenderMode(6);
                        c3d.setRenderModeOnContextMenu(6);
                        break;
                    case RENDERMODE_COPLANARITY_HEATMAP:
                        c3d.setRenderMode(7);
                        c3d.setRenderModeOnContextMenu(7);
                        break;
                    case RENDERMODE_WIREFRAME:
                        c3d.setRenderMode(-1);
                        c3d.setRenderModeOnContextMenu(-1);
                        break;
                    case CONDLINE_TO_LINE:
                        vm.condlineToLine();
                        break;
                    case LINE_TO_CONDLINE:
                        vm.lineToCondline();
                        break;
                    case QUICK_MOVE:
                    {
                        // This is the same as a switch to move mode
                        final Manipulator m = modeMove(df, vm);
                        // Adjust to world or keep local coordinates
                        adjustManipulatorToWorld();
                        // Now start quick translation
                        m.resetTranslation();
                        c3d.setQuicklyTransforming(true);
                        win.setWorkingLayer(ManipulatorAxisMode.XYZ);
                        Vector2f pos = c3d.getOldMousePosition();
                        Vector4f cursorCoordinates = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen((int) pos.x, (int) pos.y);
                        c3d.getSelectionStart().set(cursorCoordinates);
                        m.startTranslation(c3d);
                        m.setXtranslate(true);
                        m.setYtranslate(true);
                        m.setZtranslate(true);
                        break;
                    }
                    case QUICK_ROTATE:
                    {
                        // This is the same as a switch to rotate mode
                        final Manipulator m = modeRotate(df, vm);
                        // Adjust to world or keep local coordinates
                        adjustManipulatorToWorld();
                        // Now start quick translation
                        m.resetTranslation();
                        m.initRotateArrows();
                        c3d.setQuicklyTransforming(true);
                        win.setWorkingLayer(ManipulatorAxisMode.NONE);
                        Vector2f pos = c3d.getOldMousePosition();
                        Vector4f cursorCoordinates = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen((int) pos.x, (int) pos.y);
                        c3d.getSelectionStart().set(cursorCoordinates);
                        m.startTranslation(c3d);
                        m.setVrotate(true);
                        break;
                    }
                    case QUICK_SCALE:
                    {
                        // This is the same as a switch to scale mode
                        final Manipulator m = modeScale(df, vm);
                        // Adjust to world or keep local coordinates
                        adjustManipulatorToWorld();
                        // Now start quick translation
                        m.resetTranslation();
                        c3d.setQuicklyTransforming(true);
                        win.setWorkingLayer(ManipulatorAxisMode.XYZ);
                        Vector2f pos = c3d.getOldMousePosition();
                        Vector4f cursorCoordinates = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen((int) pos.x, (int) pos.y);
                        c3d.getSelectionStart().set(cursorCoordinates);
                        m.startTranslation(c3d);
                        m.setXscale(true);
                        m.setYscale(true);
                        m.setZscale(true);
                        break;
                    }
                    case QUICK_LOCK_XY:
                    {
                        if (!c3d.isQuicklyTransforming()) break;
                        final Manipulator m = c3d.getManipulator();
                        if (m.isXtranslate() || m.isYtranslate() || m.isZtranslate()) {
                            win.setWorkingLayer(ManipulatorAxisMode.XY);
                            m.setXtranslate(true);
                            m.setYtranslate(true);
                            m.setZtranslate(false);
                        }

                        if (m.isXscale() || m.isYscale() || m.isZscale()) {
                            win.setWorkingLayer(ManipulatorAxisMode.XY);
                            m.setXscale(true);
                            m.setYscale(true);
                            m.setZscale(false);
                        }

                        break;
                    }
                    case QUICK_LOCK_XZ:
                    {
                        if (!c3d.isQuicklyTransforming()) break;
                        final Manipulator m = c3d.getManipulator();
                        if (m.isXtranslate() || m.isYtranslate() || m.isZtranslate()) {
                            win.setWorkingLayer(ManipulatorAxisMode.XZ);
                            m.setXtranslate(true);
                            m.setYtranslate(false);
                            m.setZtranslate(true);
                        }

                        if (m.isXscale() || m.isYscale() || m.isZscale()) {
                            win.setWorkingLayer(ManipulatorAxisMode.XZ);
                            m.setXscale(true);
                            m.setYscale(false);
                            m.setZscale(true);
                        }

                        break;
                    }
                    case QUICK_LOCK_YZ:
                    {
                        if (!c3d.isQuicklyTransforming()) break;
                        final Manipulator m = c3d.getManipulator();
                        if (m.isXtranslate() || m.isYtranslate() || m.isZtranslate()) {
                            win.setWorkingLayer(ManipulatorAxisMode.YZ);
                            m.setXtranslate(false);
                            m.setYtranslate(true);
                            m.setZtranslate(true);
                        }

                        if (m.isXscale() || m.isYscale() || m.isZscale()) {
                            win.setWorkingLayer(ManipulatorAxisMode.YZ);
                            m.setXscale(false);
                            m.setYscale(true);
                            m.setZscale(true);
                        }

                        break;
                    }
                    case QUICK_LOCK_X:
                    {
                        if (!c3d.isQuicklyTransforming()) break;
                        final Manipulator m = c3d.getManipulator();
                        if (m.isXtranslate() || m.isYtranslate() || m.isZtranslate()) {
                            win.setWorkingLayer(ManipulatorAxisMode.X);
                            m.setXtranslate(true);
                            m.setYtranslate(false);
                            m.setZtranslate(false);
                        }

                        if (m.isXrotate() || m.isYrotate() || m.isZrotate() || m.isVrotate()) {
                            win.setWorkingLayer(ManipulatorAxisMode.X);
                            m.setXrotate(true);
                            m.setYrotate(false);
                            m.setZrotate(false);
                            m.setVrotate(false);
                        }

                        if (m.isXscale() || m.isYscale() || m.isZscale()) {
                            win.setWorkingLayer(ManipulatorAxisMode.X);
                            m.setXscale(true);
                            m.setYscale(false);
                            m.setZscale(false);
                        }

                        break;
                    }
                    case QUICK_LOCK_Y:
                    {
                        if (!c3d.isQuicklyTransforming()) break;
                        final Manipulator m = c3d.getManipulator();
                        if (m.isXtranslate() || m.isYtranslate() || m.isZtranslate()) {
                            win.setWorkingLayer(ManipulatorAxisMode.Y);
                            m.setXtranslate(false);
                            m.setYtranslate(true);
                            m.setZtranslate(false);
                            m.setVrotate(false);
                        }

                        if (m.isXrotate() || m.isYrotate() || m.isZrotate() || m.isVrotate()) {
                            win.setWorkingLayer(ManipulatorAxisMode.Y);
                            m.setXrotate(false);
                            m.setYrotate(true);
                            m.setZrotate(false);
                            m.setVrotate(false);
                        }

                        if (m.isXscale() || m.isYscale() || m.isZscale()) {
                            win.setWorkingLayer(ManipulatorAxisMode.Y);
                            m.setXscale(false);
                            m.setYscale(true);
                            m.setZscale(false);
                        }

                        break;
                    }
                    case QUICK_LOCK_Z:
                    {
                        if (!c3d.isQuicklyTransforming()) break;
                        final Manipulator m = c3d.getManipulator();
                        if (m.isXtranslate() || m.isYtranslate() || m.isZtranslate()) {
                            win.setWorkingLayer(ManipulatorAxisMode.Z);
                            m.setXtranslate(false);
                            m.setYtranslate(false);
                            m.setZtranslate(true);
                        }

                        if (m.isXrotate() || m.isYrotate() || m.isZrotate() || m.isVrotate()) {
                            win.setWorkingLayer(ManipulatorAxisMode.Z);
                            m.setXrotate(false);
                            m.setYrotate(false);
                            m.setZrotate(true);
                            m.setVrotate(false);
                        }

                        if (m.isXscale() || m.isYscale() || m.isZscale()) {
                            win.setWorkingLayer(ManipulatorAxisMode.Z);
                            m.setXscale(false);
                            m.setYscale(false);
                            m.setZscale(true);
                        }

                        break;
                    }
                    case TRIANGLE_TO_QUAD:
                        RectifierSettings rectifierSettings = new RectifierSettings();
                        rectifierSettings.setScope(1);
                        rectifierSettings.setNoBorderedQuadToRectConversation(true);
                        vm.rectify(rectifierSettings, true, true);
                        break;
                    case QUAD_TO_TRIANGLE:
                        vm.splitQuads(true);
                        break;
                    case EDGER2:
                        MiscToolItem.edger2();
                        pressedKeyCodes.clear();
                        break;
                    }
                }
            } else if (keyEventType == SWT.KeyUp) {
                NLogger.debug(KeyStateManager.class, "[Key ({0}) up]", keyCode); //$NON-NLS-1$
                pressedKeyCodes.remove(keyCode);
                setKeyState(keyCode, false);
                final boolean tmpCtrlPressed = (event.stateMask & SWT.CTRL) != 0;
                final boolean tmpAltPressed = (event.stateMask & SWT.ALT) != 0;
                final boolean tmpShiftPressed = (event.stateMask & SWT.SHIFT) != 0;
                final StringBuilder sb = new StringBuilder();
                sb.append(keyCode);
                sb.append(tmpCtrlPressed ? CTRL_STRING : ""); //$NON-NLS-1$
                sb.append(tmpAltPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(tmpShiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
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
                            break;
                        }
                    default:
                        break;
                    }
                }
            }

            // Synchronise key state with other composites
            List<OpenGLRenderer> r = Editor3DWindow.getRenders();
            for (OpenGLRenderer renderer : r) {
                renderer.getC3D().getKeys().synchronise(this);
            }
        } else {
            // Logic for CompositePrimitive
            if (keyEventType == SWT.KeyDown && !pressedKeyCodes.contains(keyCode)) {
                NLogger.debug(KeyStateManager.class, "[Key ({0}) down]", keyCode); //$NON-NLS-1$
                final boolean tmpCtrlPressed = (event.stateMask & SWT.CTRL) != 0;
                final boolean tmpAltPressed = (event.stateMask & SWT.ALT) != 0;
                final boolean tmpShiftPressed = (event.stateMask & SWT.SHIFT) != 0;
                final boolean tmpCmdPressed = (event.stateMask & SWT.COMMAND) != 0;
                final StringBuilder sb = new StringBuilder();
                sb.append(keyCode);
                sb.append(tmpCtrlPressed ? CTRL_STRING : ""); //$NON-NLS-1$
                sb.append(tmpAltPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(tmpShiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(tmpCmdPressed ? CMD_STRING : ""); //$NON-NLS-1$
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
                        break;
                    }
                    case RMB:
                    {
                        if (!Cocoa.IS_COCOA) {
                            cp.getCanvas().forceFocus();
                            Event mouseEvent = new Event();
                            mouseEvent.type = SWT.MouseDown;
                            mouseEvent.button = MouseButton.RIGHT;
                            Vector2f mpos = cp.getMousePosition();
                            mouseEvent.x = (int) mpos.x;
                            mouseEvent.y = (int) mpos.y;
                            cp.mouseDown(mouseEvent);
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
                final boolean tmpCtrlPressed = (event.stateMask & SWT.CTRL) != 0;
                final boolean tmpAltPressed = (event.stateMask & SWT.ALT) != 0;
                final boolean tmpShiftPressed = (event.stateMask & SWT.SHIFT) != 0;
                final StringBuilder sb = new StringBuilder();
                sb.append(keyCode);
                sb.append(tmpCtrlPressed ? CTRL_STRING : ""); //$NON-NLS-1$
                sb.append(tmpAltPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
                sb.append(tmpShiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
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
                        break;
                    }
                    case RMB:
                    {
                        if (!Cocoa.IS_COCOA) {
                            cp.getCanvas().forceFocus();
                            Event mouseEvent = new Event();
                            mouseEvent.type = SWT.MouseUp;
                            mouseEvent.button = MouseButton.RIGHT;
                            Vector2f mpos = cp.getMousePosition();
                            mouseEvent.x = (int) mpos.x;
                            mouseEvent.y = (int) mpos.y;
                            cp.mouseUp(mouseEvent);
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
                        break;
                    }
                    default:
                        break;
                    }
                }
            }
        }
    }

    private void adjustManipulatorToWorld() {
        if (ManipulatorScopeToolItem.getTransformationScope() == ManipulatorScope.GLOBAL) {
            Vector4f t = new Vector4f(c3d.getManipulator().getPosition());
            BigDecimal[] tP = c3d.getManipulator().getAccuratePosition();
            c3d.getManipulator().reset();
            c3d.getManipulator().getPosition().set(t);
            c3d.getManipulator().setAccuratePosition(tP[0], tP[1], tP[2]);

            ManipulatorScopeToolItem.setTransformationScope(ManipulatorScope.LOCAL);
        }
    }

    private Manipulator modeSelect(final DatFile df, final VertexManager vm) {
        return modeSwitch(df, vm, WorkingMode.SELECT);
    }

    private Manipulator modeMove(final DatFile df, final VertexManager vm) {
        return modeSwitch(df, vm, WorkingMode.MOVE);
    }

    private Manipulator modeRotate(final DatFile df, final VertexManager vm) {
        return modeSwitch(df, vm, WorkingMode.ROTATE);
    }

    private Manipulator modeScale(final DatFile df, final VertexManager vm) {
        return modeSwitch(df, vm, WorkingMode.SCALE);
    }

    private Manipulator modeCombined(final DatFile df, final VertexManager vm) {
        return modeSwitch(df, vm, WorkingMode.COMBINED);
    }

    private Manipulator modeSwitch(final DatFile df, final VertexManager vm, final WorkingMode wm) {
        final Manipulator m = c3d.getManipulator();
        m.applyTranslation(c3d);
        MouseActions.checkSyncEditMode(vm, df);
        TransformationModeToolItem.setWorkingAction(wm);
        AddToolItem.disableAddAction();
        return m;
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
        default:
            break;
        }
    }

    private void synchronise(KeyStateManager ksm) {
        this.altPressed = ksm.altPressed;
        this.shiftPressed = ksm.shiftPressed;
        this.ctrlPressed = ksm.ctrlPressed;
        this.cmdPressed = ksm.cmdPressed;
    }

    public static Set<String> getReservedKeyCodes() {
        return reservedKeyCodes;
    }

    public static Map<String, Task> getTaskmap() {
        return taskMap;
    }

    public static Map<String, TextTask> getTextTaskmap() {
        return textTaskMap;
    }

    public static Map<Task, String> getTaskKeymap() {
        return taskKeyMap;
    }

    public static Map<TextTask, String> getTextTaskKeymap() {
        return textTaskKeyMap;
    }

    private static void addTask(Task t, int keyCode) {
        addTask(t, SWT.NONE, keyCode);
    }

    private static void addTask(TextTask t, int keyCode) {
        addTask(t, SWT.NONE, keyCode);
    }

    private static void addTask(Task t, int stateMask, int keyCode) {
        String[] s = getStrings(keyCode, stateMask);
        taskMap.put(s[0], t);
        taskKeyMap.put(t, s[1]);
    }

    private static void addTask(TextTask t, int stateMask, int keyCode) {
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
        sb.append(ctrlPressed ? CTRL_STRING : ""); //$NON-NLS-1$
        sb.append(altPressed ? "+Alt" : ""); //$NON-NLS-1$//$NON-NLS-2$
        sb.append(shiftPressed ? "+Shift" : ""); //$NON-NLS-1$//$NON-NLS-2$
        sb.append(cmdPressed ? CMD_STRING : ""); //$NON-NLS-1$
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
        btn.addMouseMoveListener(e ->
            btn.setToolTipText(text + " (" + textTaskKeyMap.get(t) +")") //$NON-NLS-1$ //$NON-NLS-2$
        );
    }

    public static void addTooltipText(final NButton btn, final String text, final Task t) {
        btn.addMouseMoveListener(e ->
            btn.setToolTipText(text + " (" + taskKeyMap.get(t) +")") //$NON-NLS-1$ //$NON-NLS-2$
        );
    }

    public static void addKeyText(final MenuItem mntm, final String text, final Task t) {
        mntm.setText(text + "\t" + taskKeyMap.get(t)); //$NON-NLS-1$
        mntm.addArmListener(e ->
            mntm.setText(text + "\t" + taskKeyMap.get(t)) //$NON-NLS-1$
        );
    }

    public static void addKeyText(final MenuItem mntm, final String text, final TextTask t) {
        mntm.setText(text + "\t" + textTaskKeyMap.get(t)); //$NON-NLS-1$
        mntm.addArmListener(e ->
            mntm.setText(text + "\t" + textTaskKeyMap.get(t)) //$NON-NLS-1$
        );
    }

    public static void addKeyText(final MenuItem mntm, final String text) {
        mntm.setText(text);
    }

    public static void addTooltipText(NButton btn, final String text) {
        btn.setToolTipText(text);
    }

    public static String getMapKey(Task t1) {
        for (Entry<String, Task> entry : taskMap.entrySet()) {
            String k = entry.getKey();
            if (entry.getValue() == t1) {
                return k;
            }
        }
        return null;
    }

    public static String getMapKey(TextTask t2) {
        for (Entry<String, TextTask> entry : textTaskMap.entrySet()) {
            String k = entry.getKey();
            if (entry.getValue() == t2) {
                return k;
            }
        }
        return null;
    }

    public static void changeKey(String newKey, String keyString, Task t) {
        for (Iterator<Entry<String, Task>> it = taskMap.entrySet().iterator(); it.hasNext();) {
            Entry<String, Task> entry = it.next();
            if (entry.getValue() == t) {
                it.remove();
            }
        }

        taskMap.put(newKey, t);
        taskKeyMap.put(t, keyString);
    }

    public static void changeKey(String newKey, String keyString, TextTask t) {
        for (Iterator<Entry<String, TextTask>> it = textTaskMap.entrySet().iterator(); it.hasNext();) {
            Entry<String, TextTask> entry = it.next();
            if (entry.getValue() == t) {
                it.remove();
            }
        }

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
        Matrix4f viewportRotation = c3d.getRotation();
        Matrix4f viewportTranslation = c3d.getTranslation();
        Matrix4f oldViewportTranslation = new Matrix4f();
        Matrix4f.load(c3d.getTranslation(), oldViewportTranslation);
        Vector4f xAxis4fTranslation = new Vector4f(dx, 0, 0, 1.0f);
        Vector4f yAxis4fTranslation = new Vector4f(0, dy, 0, 1.0f);
        Matrix4f ovrInverse2 = Matrix4f.invert(viewportRotation, null);
        Matrix4f.transform(ovrInverse2, xAxis4fTranslation, xAxis4fTranslation);
        Matrix4f.transform(ovrInverse2, yAxis4fTranslation, yAxis4fTranslation);
        Vector3f xAxis3 = new Vector3f(xAxis4fTranslation.x, xAxis4fTranslation.y, xAxis4fTranslation.z);
        Vector3f yAxis3 = new Vector3f(yAxis4fTranslation.x, yAxis4fTranslation.y, yAxis4fTranslation.z);
        Matrix4f.load(oldViewportTranslation, viewportTranslation);
        Matrix4f.translate(xAxis3, oldViewportTranslation, viewportTranslation);
        Matrix4f.translate(yAxis3, viewportTranslation, viewportTranslation);
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

    public static void cleanupDuplicatedKeys() {
        final int maxTries = 10_000;
        final Set<String> usedKeyCodes = new HashSet<>();
        boolean foundDuplicate = true;

        int tryCount = 0;

        {
            List<Task> taskEntries = taskKeyMap.keySet().stream().toList();
            final int tasksSize = taskKeyMap.size();
            while (foundDuplicate && tryCount < maxTries) {
                foundDuplicate = false;
                tryCount++;
                usedKeyCodes.clear();
                for (int i = 0; i < tasksSize; i++) {
                    final Task task = taskEntries.get(i);
                    final String keyCode = taskMap.entrySet().stream().filter(e -> e.getValue() == task).map(Entry::getKey).findFirst().orElse(null);;
                    if (usedKeyCodes.contains(keyCode) || keyCode == null) {
                        final String originalKey = backupTaskKeyMap.get(task);
                        final String originalKeyCode = backupTaskMap.entrySet().stream().filter(e -> e.getValue() == task).map(Entry::getKey).findFirst().orElse(null);
                        if (originalKeyCode != null) {
                            foundDuplicate = true;
                            KeyStateManager.changeKey(originalKeyCode, originalKey, task);
                            taskEntries = taskEntries.reversed();
                            break;
                        }
                    }

                    usedKeyCodes.add(keyCode);
                }
            }

            if (tryCount == maxTries) {
                NLogger.error(KeyStateManager.class, "Duplicate removal failed! (taskKeyMap)"); //$NON-NLS-1$
            }
        }

        tryCount = 0;
        foundDuplicate = true;

        {
            List<TextTask> textTaskEntries = textTaskKeyMap.keySet().stream().toList();
            final int textTasksSize = textTaskKeyMap.size();
            while (foundDuplicate && tryCount < maxTries) {
                foundDuplicate = false;
                tryCount++;
                usedKeyCodes.clear();
                for (int i = 0; i < textTasksSize; i++) {
                    final TextTask task = textTaskEntries.get(i);
                    final String keyCode = textTaskMap.entrySet().stream().filter(e -> e.getValue() == task).map(Entry::getKey).findFirst().orElse(null);;
                    if (usedKeyCodes.contains(keyCode) || keyCode == null) {
                        final String originalKey = backupTextTaskKeyMap.get(task);
                        final String originalKeyCode = backupTextTaskMap.entrySet().stream().filter(e -> e.getValue() == task).map(Entry::getKey).findFirst().orElse(null);
                        if (originalKeyCode != null) {
                            foundDuplicate = true;
                            KeyStateManager.changeKey(originalKeyCode, originalKey, task);
                            textTaskEntries = textTaskEntries.reversed();
                            break;
                        }
                    }

                    usedKeyCodes.add(keyCode);
                }
            }

            if (tryCount == maxTries) {
                NLogger.error(KeyStateManager.class, "Duplicate removal failed! (textTaskKeyMap)"); //$NON-NLS-1$
            }
        }
    }
}
