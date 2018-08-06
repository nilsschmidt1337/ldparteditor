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
package org.nschmidt.ldparteditor.enums;

/**
 * Task indicator for a pressed key
 * @author nils
 *
 */
public enum Task {

    DELETE, COPY, CUT, PASTE,

    ESC,

    MODE_SELECT,
    MODE_MOVE,
    MODE_ROTATE,
    MODE_SCALE,
    MODE_COMBINED,

    COLOUR_NUMBER0,
    COLOUR_NUMBER1,
    COLOUR_NUMBER2,
    COLOUR_NUMBER3,
    COLOUR_NUMBER4,
    COLOUR_NUMBER5,
    COLOUR_NUMBER6,
    COLOUR_NUMBER7,
    COLOUR_NUMBER8,
    COLOUR_NUMBER9,

    ADD_VERTEX,
    ADD_LINE,
    ADD_TRIANGLE,
    ADD_QUAD,
    ADD_CONDLINE,
    ADD_COMMENTS,
    ADD_DISTANCE,
    ADD_PROTRACTOR,

    ZOOM_IN,
    ZOOM_OUT,

    RESET_VIEW,
    SHOW_GRID,
    SHOW_RULER,

    OBJ_VERTEX,
    OBJ_FACE,
    OBJ_LINE,
    OBJ_PRIMITIVE,

    UNDO,
    REDO,

    SAVE,

    SELECT_ALL,
    SELECT_NONE,
    SELECT_ALL_WITH_SAME_COLOURS,

    SELECT_CONNECTED,
    SELECT_TOUCHING,

    SELECT_OPTION_WITH_SAME_COLOURS,

    FLIP_ROTATE_VERTICES,
    MERGE_TO_AVERAGE,
    MERGE_TO_LAST,
    SPLIT,

    LMB,
    MMB,
    RMB,

    INSERT_AT_CURSOR,
    MOVE_TO_AVG,

    MODE_X,
    MODE_Y,
    MODE_Z,
    MODE_XY,
    MODE_XZ,
    MODE_YZ,
    MODE_XYZ,

    TRANSFORM_UP,
    TRANSFORM_RIGHT,
    TRANSFORM_DOWN,
    TRANSFORM_LEFT,

    TRANSFORM_UP_COPY,
    TRANSFORM_RIGHT_COPY,
    TRANSFORM_DOWN_COPY,
    TRANSFORM_LEFT_COPY,

    TRANSLATE_UP,
    TRANSLATE_RIGHT,
    TRANSLATE_DOWN,
    TRANSLATE_LEFT,

    MOVE_ADJACENT_DATA,

    SWAP_WINDING,

    CLOSE_VIEW
}
