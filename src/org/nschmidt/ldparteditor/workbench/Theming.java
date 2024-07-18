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
package org.nschmidt.ldparteditor.workbench;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.enumtype.Colour;
import org.nschmidt.ldparteditor.enumtype.TextEditorColour;
import org.nschmidt.ldparteditor.logger.NLogger;

@SuppressWarnings("java:S2696")
public enum Theming implements ThemeColours {
    DEFAULT {
        @Override
        public void overrideColours() {
            super.overrideColours();
            NLogger.debug(Theming.class, "Restoring default theme."); //$NON-NLS-1$
            WorkbenchManager.getUserSettingState().loadColourSettings();
            WorkbenchManager.getUserSettingState().saveColours();
            WorkbenchManager.getThemeSettingState().saveColours();
        }
    },

    DRACULA {
        @Override
        public void overrideColours() {
            super.overrideColours();
            NLogger.debug(Theming.class, "Loading Dracula theme."); //$NON-NLS-1$
            WorkbenchManager.getUserSettingState().loadColourSettings();

            TextEditorColour.loadTextBackground(getBgColor());
            TextEditorColour.loadTextForeground(getFgColor());

            Colour.backgroundColourR = 40 / 255f;
            Colour.backgroundColourG = 42 / 255f;
            Colour.backgroundColourB = 54 / 255f;

            Colour.gridColourR = 248 / 255f;
            Colour.gridColourG = 248 / 255f;
            Colour.gridColourB = 242 / 255f;

            Colour.grid10ColourR = Colour.gridColourR;
            Colour.grid10ColourG = Colour.gridColourG;
            Colour.grid10ColourB = Colour.gridColourB;

            Colour.primitiveBackgroundColourR = Colour.backgroundColourR;
            Colour.primitiveBackgroundColourG = Colour.backgroundColourG;
            Colour.primitiveBackgroundColourB = Colour.backgroundColourB;

            Colour.textColourR = Colour.gridColourR;
            Colour.textColourG = Colour.gridColourG;
            Colour.textColourB = Colour.gridColourB;

            Colour.textColourAltR = Colour.gridColourR;
            Colour.textColourAltG = Colour.gridColourG;
            Colour.textColourAltB = Colour.gridColourB;

            Colour.textColourDefaultR = Colour.gridColourR;
            Colour.textColourDefaultG = Colour.gridColourG;
            Colour.textColourDefaultB = Colour.gridColourB;

            TextEditorColour.loadLineHighlightBackground(SWTResourceManager.getColor(68, 71, 90));
            TextEditorColour.loadLineHighlightSelectedBackground(SWTResourceManager.getColor(68, 71, 90));
            TextEditorColour.loadLineCommentFont(SWTResourceManager.getColor(98, 114, 164));
            TextEditorColour.loadTextForegroundHidden(SWTResourceManager.getColor(124, 124, 121));

            TextEditorColour.loadLinePrimaryFont(SWTResourceManager.getColor(80, 250, 123));
            TextEditorColour.loadLineSecondaryFont(SWTResourceManager.getColor(255, 121, 198));
            TextEditorColour.loadLineQuadFont(SWTResourceManager.getColor(255, 184, 108));

            TextEditorColour.loadLineErrorUnderline(SWTResourceManager.getColor(255, 85, 85));
            TextEditorColour.loadLineWarningUnderline(SWTResourceManager.getColor(241, 250, 140));

            TextEditorColour.loadLineBoxFont(getFgColor());
            TextEditorColour.loadLineHintUnderline(SWTResourceManager.getColor(189, 147, 249));

            // TODO Needs implementation!

            WorkbenchManager.getThemeSettingState().saveColours();
        }
    },

    DARK {
        @Override
        public void overrideColours() {
            super.overrideColours();

            // TODO Needs implementation!
        }
    },

    LIGHT {
        @Override
        public void overrideColours() {
            super.overrideColours();
            NLogger.debug(Theming.class, "Loading Light theme."); //$NON-NLS-1$
            WorkbenchManager.getUserSettingState().loadColourSettings();

            TextEditorColour.loadLineHighlightBackground(SWTResourceManager.getColor(190, 190, 210));

            // TODO Needs implementation!

            WorkbenchManager.getThemeSettingState().saveColours();
        }
    };

    private static Theming currentTheme = DEFAULT;

    public static Theming getCurrentTheme() {
        return currentTheme;
    }

    @SuppressWarnings("java:S3066")
    public static void setCurrentTheme(Theming currentTheme) {
        Theming.currentTheme = currentTheme;
    }

    public static Color getBgColor() {
        if (currentTheme == DRACULA) {
            return SWTResourceManager.getColor(40, 42, 54);
        } else if (currentTheme == LIGHT) {
            return SWTResourceManager.getColor(255, 255, 255);
        }

        return SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND);
    }

    public static Color getFgColor() {
        if (currentTheme == DRACULA) {
            return SWTResourceManager.getColor(248, 248, 242);
        } else if (currentTheme == LIGHT) {
            return SWTResourceManager.getColor(0, 0, 0);
        }

        return SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND);
    }

    public static Composite composite(Composite parent, int style) {
        final Composite result = new Composite(parent, style);
        if (currentTheme != DEFAULT) {
            setBgColor(result);
        }

        return result;
    }

    public static Label label(Composite parent, int style) {
        final Label result = new Label(parent, style);
        if (currentTheme != DEFAULT) {
            setBgColor(result);
            setFgColor(result);
        }

        return result;
    }

    public static CTabFolder cTabFolder(Composite parent, int style) {
        final CTabFolder result = new CTabFolder(parent, style);
        if (currentTheme != DEFAULT) {
            if (currentTheme == DRACULA) {
                result.setSelectionForeground(SWTResourceManager.getColor(68, 71, 90));
            } else {
                result.setSelectionForeground(getFgColor());
            }

            setBgColor(result);
            setFgColor(result);
        }

        return result;
    }

    public static Group group(Composite parent, int style) {
        final Group result = new Group(parent, style);
        if (currentTheme != DEFAULT) {
            setBgColor(result);
            setFgColor(result);
        }

        return result;
    }

    public static SashForm shashForm(Composite parent, int style) {
        final SashForm result = new SashForm(parent, style);
        if (currentTheme != DEFAULT) {
            setBgColor(result);
            setFgColor(result);
        }

        return result;
    }

    public static Text text(Composite parent, int style) {
        final Text result = new Text(parent, style);
        if (currentTheme != DEFAULT) {
            setBgColor(result);
            setFgColor(result);
        }

        return result;
    }

    public static Tree tree(Composite parent, int columnCount) {
        final Tree result = new Tree(parent, columnCount);
        if (currentTheme != DEFAULT) {
            setBgColor(result);
            setFgColor(result);

            if (currentTheme == DRACULA) {
                result.setHeaderBackground(SWTResourceManager.getColor(68, 71, 90));
            } else {
                result.setHeaderBackground(getBgColor());
            }

            result.setHeaderForeground(getFgColor());
        }

        return result;
    }

    public static Combo combo(Composite parent, int style) {
        final Combo result = new Combo(parent, style);
        if (currentTheme != DEFAULT) {
            setBgColor(result);
            setFgColor(result);
        }

        return result;
    }

    private static void setBgColor(Control ctrl) {
        ctrl.setBackground(getBgColor());
    }

    private static void setFgColor(Control ctrl) {
        ctrl.setForeground(getFgColor());
    }

    @Override
    public void overrideColours() {
        // Do nothing.
    }
}
