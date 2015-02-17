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

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.nschmidt.ldparteditor.enums.Colour;
import org.nschmidt.ldparteditor.enums.Font;

/**
 * The resource manager, which returns pictures, sounds and so on..
 *
 * @author nils
 *
 */
public enum ResourceManager {
    INSTANCE;

    /** The hash map, which stores already loaded images in-memory. */
    private static HashMap<String, Image> imageMap = new HashMap<String, Image>();

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
        Image img = null;
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
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        imageMap.remove(name);
    }
}
