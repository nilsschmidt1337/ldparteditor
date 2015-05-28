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

    MERGE_TO_AVERAGE,
    MERGE_TO_LAST,
    SPLIT,

    LMB,
    MMB,
    RMB,


}
