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
 * A enum class for different 3D manipulation modes
 *
 * @author nils
 *
 */
public enum WorkingMode {
    INSTANCE;

    /** The VERTICES mode */
    public static final int VERTICES = 0;
    /** The SUBFILES mode */
    public static final int SUBFILES = 1;
    /** The LINES mode */
    public static final int LINES = 2;
    /** The FACES mode */
    public static final int FACES = 3;

    /** The SELECT mode */
    public static final int SELECT = 4;
    /** The MOVE mode */
    public static final int MOVE = 5;
    /** The ROTATE mode */
    public static final int ROTATE = 6;
    /** The SCALE mode */
    public static final int SCALE = 7;
    /** The COMBINED mode */
    public static final int COMBINED = 8;

    /** The LOCAL mode */
    public static final int LOCAL = 9;
    /** The GLOBAL mode */
    public static final int GLOBAL = 10;
}
