/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.swt;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * Utility class for managing OS resources associated with SWT controls such as
 * colors, fonts, etc.
 * <p>
 * !!! IMPORTANT !!! Application code must explicitly invoke the
 * <code>dispose()</code> method to release the operating system resources
 * managed by cached objects when those objects and OS resources are no longer
 * needed (e.g. on application shutdown)
 * <p>
 * This class may be freely distributed as part of any application or plugin.
 * <p>
 *
 * @author scheglov_ke
 * @author Dan Rubel
 */
public class SWTResourceManager {
    // //////////////////////////////////////////////////////////////////////////
    //
    // Color
    //
    // //////////////////////////////////////////////////////////////////////////
    private static Map<RGB, Color> m_colorMap = new HashMap<>();

    /**
     * Returns the system {@link Color} matching the specific ID.
     *
     * @param systemColorID
     *            the ID value for the color
     * @return the system {@link Color} matching the specific ID
     */
    public static Color getColor(int systemColorID) {
        Display display = Display.getCurrent();
        return display.getSystemColor(systemColorID);
    }

    /**
     * Returns a {@link Color} given its red, green and blue component values.
     *
     * @param r
     *            the red component of the color
     * @param g
     *            the green component of the color
     * @param b
     *            the blue component of the color
     * @return the {@link Color} matching the given red, green and blue
     *         component values
     */
    public static Color getColor(int r, int g, int b) {
        // Validation for RGB argument range
        int clampR = Math.max(0, Math.min(255, r));
        int clampG = Math.max(0, Math.min(255, g));
        int clampB = Math.max(0, Math.min(255, b));
        if (clampR != r || clampG != g || clampB != b) {
            NLogger.error(SWTResourceManager.class, new IllegalArgumentException("Illegal argument on getColor (R:" + r + " G:" + g + " B:" + b + ")")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        return getColor(new RGB(clampR, clampG, clampB));
    }

    /**
     * Returns a {@link Color} given its RGB value.
     *
     * @param rgb
     *            the {@link RGB} value of the color
     * @return the {@link Color} matching the RGB value
     */
    public static Color getColor(RGB rgb) {
        Color color = m_colorMap.get(rgb);
        if (color == null) {
            Display display = Display.getCurrent();
            color = new Color(display, rgb);
            m_colorMap.put(rgb, color);
        }
        return color;
    }

    /**
     * Dispose of all the cached {@link Color}'s.
     */
    private static void disposeColors() {
        for (Color color : m_colorMap.values()) {
            color.dispose();
        }
        m_colorMap.clear();
    }

    // //////////////////////////////////////////////////////////////////////////
    //
    // Font
    //
    // //////////////////////////////////////////////////////////////////////////
    /**
     * Maps font names to fonts.
     */
    private static Map<String, Font> m_fontMap = new HashMap<>();

    /**
     * Returns a {@link Font} based on its name, height and style.
     *
     * @param name
     *            the name of the font
     * @param height
     *            the height of the font
     * @param style
     *            the style of the font
     * @return {@link Font} The font matching the name, height and style
     */
    public static Font getFont(String name, int height, int style) {
        return getFont(name, height, style, false, false);
    }

    /**
     * Returns a {@link Font} based on its name, height and style.
     * Windows-specific strikeout and underline flags are also supported.
     *
     * @param name
     *            the name of the font
     * @param size
     *            the size of the font
     * @param style
     *            the style of the font
     * @param strikeout
     *            the strikeout flag (warning: Windows only)
     * @param underline
     *            the underline flag (warning: Windows only)
     * @return {@link Font} The font matching the name, height, style, strikeout
     *         and underline
     */
    private static Font getFont(String name, int size, int style, boolean strikeout, boolean underline) {
        String fontName = name + '|' + size + '|' + style + '|' + strikeout + '|' + underline;
        Font font = m_fontMap.get(fontName);
        if (font == null) {
            FontData fontData = new FontData(name, size, style);
            if (strikeout || underline) {
                try {
                    Class<?> logFontClass = Class.forName("org.eclipse.swt.internal.win32.LOGFONT"); //$NON-NLS-1$
                    Object logFont = FontData.class.getField("data").get(fontData); //$NON-NLS-1$
                    if (logFont != null && logFontClass != null) {
                        if (strikeout) {
                            logFontClass.getField("lfStrikeOut").set(logFont, Byte.valueOf((byte) 1)); //$NON-NLS-1$
                        }
                        if (underline) {
                            logFontClass.getField("lfUnderline").set(logFont, Byte.valueOf((byte) 1)); //$NON-NLS-1$
                        }
                    }
                } catch (Throwable e) {
                    System.err.println("Unable to set underline or strikeout" + " (probably on a non-Windows platform). " + e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            font = new Font(Display.getCurrent(), fontData);
            m_fontMap.put(fontName, font);
        }
        return font;
    }

    /**
     * Dispose all of the cached {@link Font}'s.
     */
    private static void disposeFonts() {
        // clear fonts
        for (Font font : m_fontMap.values()) {
            font.dispose();
        }
        m_fontMap.clear();
    }

    // //////////////////////////////////////////////////////////////////////////
    //
    // General
    //
    // //////////////////////////////////////////////////////////////////////////
    /**
     * Dispose of cached objects and their underlying OS resources. This should
     * only be called when the cached objects are no longer needed (e.g. on
     * application shutdown).
     */
    public static void dispose() {
        disposeColors();
        disposeFonts();
    }
}