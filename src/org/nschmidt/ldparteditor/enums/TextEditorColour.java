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

import org.eclipse.swt.graphics.Color;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * A colour "constants" provider for the EditorTextWindow class
 *
 * @author nils
 *
 */
public enum TextEditorColour {
    INSTANCE;

    /** The background of hightlighted text lines */
    private static Color lineHighlightBackground = SWTResourceManager.getColor(235, 236, 255);
    /** The background of hightlighted selected text lines */
    private static Color lineHighlightSelectedBackground = SWTResourceManager.getColor(242, 240, 240);
    /** The underline colour of line errors */
    private static Color lineErrorUnderline = SWTResourceManager.getColor(235, 30, 30);
    /** The underline colour of line warnings */
    private static Color lineWarningUnderline = SWTResourceManager.getColor(235, 235, 30);
    /** The underline colour of line hints */
    private static Color lineHintUnderline = SWTResourceManager.getColor(30, 30, 235);
    /** The font colour of line comments */
    private static Color lineCommentFont = SWTResourceManager.getColor(10, 10, 255);
    /** The primary font colour of other line data */
    private static Color linePrimaryFont = SWTResourceManager.getColor(116, 129, 90);
    /** The secondary font colour of other line data */
    private static Color lineSecondaryFont = SWTResourceManager.getColor(120, 83, 67);
    /** The main font colour of the colour attribute */
    private static Color lineColourAttrFont = SWTResourceManager.getColor(0, 0, 200);
    /** The main font colour of the quad number 4 */
    private static Color lineQuadFont = SWTResourceManager.getColor(187, 24, 41);
    /** The main font colour of font borders */
    private static Color lineBoxFont = SWTResourceManager.getColor(24, 24, 24);
    /** The main background colour of the text */
    private static Color textBackground = SWTResourceManager.getColor(255, 255, 255);
    /** The main foreground colour of the text */
    private static Color textForeground = SWTResourceManager.getColor(0, 0, 0);
    /** The main foreground colour of the text from hidden objects */
    private static Color textForegroundHidden = SWTResourceManager.getColor(240, 240, 240);

    /**
     * Disposes all colours
     */
    public static void dispose() {
        getLineErrorUnderline().dispose();
        getLineWarningUnderline().dispose();
        getLineHintUnderline().dispose();
        getLineHighlightBackground().dispose();
        getLineHighlightSelectedBackground().dispose();
        getLineCommentFont().dispose();
        getLinePrimaryFont().dispose();
        getLineSecondaryFont().dispose();
        getLineQuadFont().dispose();
        getLineColourAttrFont().dispose();
        getLineBoxFont().dispose();
        getTextBackground().dispose();
        getTextForeground().dispose();
        getTextForegroundHidden().dispose();
    }

    public static Color getLineHighlightBackground() {
        return lineHighlightBackground;
    }

    public static void loadLineHighlightBackground(Color lineHighlightBackground) {
        TextEditorColour.lineHighlightBackground = lineHighlightBackground;
    }

    public static Color getLineHighlightSelectedBackground() {
        return lineHighlightSelectedBackground;
    }

    public static void loadLineHighlightSelectedBackground(Color lineHighlightSelectedBackground) {
        TextEditorColour.lineHighlightSelectedBackground = lineHighlightSelectedBackground;
    }

    public static Color getLineErrorUnderline() {
        return lineErrorUnderline;
    }

    public static void loadLineErrorUnderline(Color lineErrorUnderline) {
        TextEditorColour.lineErrorUnderline = lineErrorUnderline;
    }

    public static Color getLineWarningUnderline() {
        return lineWarningUnderline;
    }

    public static void loadLineWarningUnderline(Color lineWarningUnderline) {
        TextEditorColour.lineWarningUnderline = lineWarningUnderline;
    }

    public static Color getLineHintUnderline() {
        return lineHintUnderline;
    }

    public static void loadLineHintUnderline(Color lineHintUnderline) {
        TextEditorColour.lineHintUnderline = lineHintUnderline;
    }

    public static Color getLineCommentFont() {
        return lineCommentFont;
    }

    public static void loadLineCommentFont(Color lineCommentFont) {
        TextEditorColour.lineCommentFont = lineCommentFont;
    }

    public static Color getLinePrimaryFont() {
        return linePrimaryFont;
    }

    public static void loadLinePrimaryFont(Color linePrimaryFont) {
        TextEditorColour.linePrimaryFont = linePrimaryFont;
    }

    public static Color getLineSecondaryFont() {
        return lineSecondaryFont;
    }

    public static void loadLineSecondaryFont(Color lineSecondaryFont) {
        TextEditorColour.lineSecondaryFont = lineSecondaryFont;
    }

    public static Color getLineColourAttrFont() {
        return lineColourAttrFont;
    }

    public static void loadLineColourAttrFont(Color lineColourAttrFont) {
        TextEditorColour.lineColourAttrFont = lineColourAttrFont;
    }

    public static Color getLineQuadFont() {
        return lineQuadFont;
    }

    public static void loadLineQuadFont(Color lineQuadFont) {
        TextEditorColour.lineQuadFont = lineQuadFont;
    }

    public static Color getLineBoxFont() {
        return lineBoxFont;
    }

    public static void loadLineBoxFont(Color lineBoxFont) {
        TextEditorColour.lineBoxFont = lineBoxFont;
    }

    public static Color getTextBackground() {
        return textBackground;
    }

    public static void loadTextBackground(Color textBackground) {
        TextEditorColour.textBackground = textBackground;
    }

    public static Color getTextForeground() {
        return textForeground;
    }

    public static void loadTextForeground(Color textForeground) {
        TextEditorColour.textForeground = textForeground;
    }

    public static Color getTextForegroundHidden() {
        return textForegroundHidden;
    }

    public static void loadTextForegroundHidden(Color textForegroundHidden) {
        TextEditorColour.textForegroundHidden = textForegroundHidden;
    }
}
