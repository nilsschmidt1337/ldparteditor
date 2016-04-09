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

import org.eclipse.wb.swt.SWTResourceManager;

/**
 * A colour "constants" provider for the EditorTextWindow class
 *
 * @author nils
 *
 */
public enum Colour {
    INSTANCE;

    /** The background of hightlighted text lines */
    public static org.eclipse.swt.graphics.Color[] line_highlight_background = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(235, 236, 255)};
    /** The background of hightlighted selected text lines */
    public static org.eclipse.swt.graphics.Color[] line_highlight_selected_background = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(242, 240, 240)};
    /** The underline colour of line errors */
    public static org.eclipse.swt.graphics.Color[] line_error_underline = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(235, 30, 30)};
    /** The underline colour of line warnings */
    public static org.eclipse.swt.graphics.Color[] line_warning_underline = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(235, 235, 30)};
    /** The underline colour of line hints */
    public static org.eclipse.swt.graphics.Color[] line_hint_underline = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(30, 30, 235)};
    /** The font colour of line comments */
    public static org.eclipse.swt.graphics.Color[] line_comment_font = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(10, 10, 255)};
    /** The primary font colour of other line data */
    public static org.eclipse.swt.graphics.Color[] line_primary_font = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(116, 129, 90)};
    /** The secondary font colour of other line data */
    public static org.eclipse.swt.graphics.Color[] line_secondary_font = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(120, 83, 67)};
    /** The main font colour of the colour attribute */
    public static org.eclipse.swt.graphics.Color[] line_colourAttr_font = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(0, 0, 200)};
    /** The main font colour of the quad number 4 */
    public static org.eclipse.swt.graphics.Color[] line_quad_font = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(187, 24, 41)};
    /** The main font colour of font borders */
    public static org.eclipse.swt.graphics.Color[] line_box_font = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(24, 24, 24)};
    /** The main background colour of the text */
    public static org.eclipse.swt.graphics.Color[] text_background = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(255, 255, 255)};
    /** The main foreground colour of the text */
    public static org.eclipse.swt.graphics.Color[] text_foreground = new org.eclipse.swt.graphics.Color[]{SWTResourceManager.getColor(0, 0, 0)};

    /**
     * Disposes all colours
     */
    public static void dispose() {
        line_error_underline[0].dispose();
        line_warning_underline[0].dispose();
        line_hint_underline[0].dispose();
        line_highlight_background[0].dispose();
        line_highlight_selected_background[0].dispose();
        line_comment_font[0].dispose();
        line_primary_font[0].dispose();
        line_secondary_font[0].dispose();
        line_quad_font[0].dispose();
        line_colourAttr_font[0].dispose();
        line_box_font[0].dispose();
        text_background[0].dispose();
        text_foreground[0].dispose();
    }

    /**
     * Saves the editor colours
     */
    public static void saveColours() {

    }

    /**
     * Loads the editor colours
     */
    public static void loadColours() {

    }

    /**
     * Saves the default editor colours
     */
    public static void saveDefaultColours() {

    }

}
