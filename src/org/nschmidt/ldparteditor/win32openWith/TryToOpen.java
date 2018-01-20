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
package org.nschmidt.ldparteditor.win32openWith;

import static org.nschmidt.ldparteditor.win32openWith.FileActionResult.DELEGATED_TO_ANOTHER_INSTANCE;
import static org.nschmidt.ldparteditor.win32openWith.FileActionResult.FILE_NOT_FOUND;

import java.io.IOException;
import java.nio.file.Paths;

import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Provides a method of emulating "Open with..."
 */
public enum TryToOpen {
    INSTANCE;

    /**
     * Tries to open a file with an LDPE instance
     * @param path the path to the file
     * @return a {@linkplain FileActionResult} which indicates if the file was not found/accessible, if it will be opened or if it was delegated to another LDPE instance
     */
    public static FileActionResult File(String path) {

        FileActionResult result = FILE_NOT_FOUND;

        // Try to call another LDPE instance
        try {
            if (path != null && new java.io.File(path).exists()) {
                try {
                    final WatchConfigDirectory wcd = new WatchConfigDirectory(Paths.get(WorkbenchManager.CONFIG_GZ).getParent(), Paths.get(path));
                    result = wcd.callAnotherLDPartEditorInstance();
                } catch (IOException ioe) {
                    NLogger.error(TryToOpen.class, ioe);
                }
            }
        } catch (SecurityException se) {
            NLogger.debug(TryToOpen.class, se);
        }

        // Listen to fileOpen calls from other LDPE instances
        if (result != DELEGATED_TO_ANOTHER_INSTANCE) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        new WatchConfigDirectory(Paths.get(WorkbenchManager.CONFIG_GZ).getParent()).waitForCall();
                    } catch (IOException ioe) {
                        NLogger.error(TryToOpen.class, ioe);
                    }
                }
            }.start();
        }

        // Return FILE_NOT_FOUND when there was no file to open
        // it returns DELEGATED_TO_ANOTHER_INSTANCE if there is another LDPE instance which will open it
        // it returns WILL_OPEN_FILE if this instance is going to open the file
        return result;
    }
}
