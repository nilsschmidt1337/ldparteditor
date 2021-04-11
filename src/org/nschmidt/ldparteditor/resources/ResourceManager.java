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
package org.nschmidt.ldparteditor.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.Font;
import org.nschmidt.ldparteditor.enums.IconSize;
import org.nschmidt.ldparteditor.helpers.LDPartEditorException;

/**
 * The resource manager, which returns pictures, sounds and so on..
 *
 * @author nils
 *
 */
public enum ResourceManager {
    INSTANCE;

    /** The hash map, which stores already loaded images in-memory. */
    private static Map<String, Image> imageMap = new HashMap<>();

    /**
     * Loads a image with a given name from the resource package. Note: By
     * convention the image name starts with "img" or "icon".
     *
     * @param name
     *            The name of the image located at
     *            org.nschmidt.ldparteditor.resources
     * @return The image as {@link org.eclipse.swt.graphics.Image}.
     */
    public static Image getImage(String name) {
        if (!name.startsWith("img")) { //$NON-NLS-1$
            return getImage(name, IconSize.getIconsize());
        } else {
            return getImage(name, 0);
        }
    }

    public static Image getImage(String name, int iconSize) {
        Image img = null;
        if (!name.startsWith("img")) { //$NON-NLS-1$
            switch (iconSize) {
            case -1:
                name = name.replace("icon8", "icon8"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon12", "icon8"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon16", "icon12"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon20", "icon16"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon24", "icon20"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon28", "icon24"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon32", "icon28"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon36", "icon32"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon40", "icon36"); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            case 0:
                // Default size.
                break;
            case 1:
                name = name.replace("icon40", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon36", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon32", "icon36"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon28", "icon32"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon24", "icon28"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon20", "icon24"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon16", "icon20"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon12", "icon16"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon8", "icon12"); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            case 2:
                name = name.replace("icon40", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon36", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon32", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon28", "icon36"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon24", "icon32"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon20", "icon28"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon16", "icon24"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon12", "icon20"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon8", "icon16"); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            case 3:
                name = name.replace("icon40", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon36", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon32", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon28", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon24", "icon36"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon20", "icon32"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon16", "icon28"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon12", "icon24"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon8", "icon20"); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            case 4:
                name = name.replace("icon40", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon36", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon32", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon28", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon24", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon20", "icon36"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon16", "icon32"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon12", "icon28"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon8", "icon24"); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            case 5:
                name = name.replace("icon40", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon36", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon32", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon28", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon24", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon20", "icon40"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon16", "icon36"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon12", "icon32"); //$NON-NLS-1$ //$NON-NLS-2$
                name = name.replace("icon8", "icon28"); //$NON-NLS-1$ //$NON-NLS-2$
                break;
            default:
                break;
            }
        }
        if (imageMap.containsKey(name)) {
            img = imageMap.get(name);
        } else {
            InputStream imgStream = ResourceManager.class.getResourceAsStream(name);
            try {
                if (imgStream != null) {
                    img = new Image(Display.getCurrent(), imgStream);
                    imageMap.put(name, img);
                }
            } catch (SWTException swt) {
            } finally {
                try {
                    if (imgStream != null) {
                        imgStream.close();
                    }
                } catch (IOException io) {
                }
            }
        }
        return img;
    }

    /**
     * Disposes all managed resources
     */
    public static void dispose() {
        // Dispose all images
        for (String s : imageMap.keySet()) {
            imageMap.get(s).dispose();
        }
        imageMap.clear();
        // Dispose all static fonts
        Font.SMALL.dispose();
        Font.SYSTEM.dispose();
        Font.MONOSPACE.dispose();
        // Dispose all colors
        Colour.dispose();
    }

    /**
     * Disposes a image specified by its name
     *
     * @param name
     *            The name of the image located at
     *            org.nschmidt.ldparteditor.resources
     */
    public static void disposeImage(String name) {
        imageMap.get(name).dispose();
        while (!imageMap.get(name).isDisposed()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new LDPartEditorException(ie);
            }
        }
        imageMap.remove(name);
    }
}
