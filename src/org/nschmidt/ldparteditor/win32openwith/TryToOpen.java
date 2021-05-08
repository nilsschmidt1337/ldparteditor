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
package org.nschmidt.ldparteditor.win32openwith;

import static org.nschmidt.ldparteditor.win32openwith.FileActionResult.DELEGATED_TO_ANOTHER_INSTANCE;
import static org.nschmidt.ldparteditor.win32openwith.FileActionResult.FILE_NOT_FOUND;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.enums.OpenInWhat;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Provides a method of emulating "Open with..."
 */
public enum TryToOpen {
    INSTANCE;

    private static DatFile datFileToOpen = null;

    /**
     * Tries to open a file with an LDPE instance
     * @param path the path to the file
     * @return a {@linkplain FileActionResult} which indicates if the file was not found/accessible, if it will be opened or if it was delegated to another LDPE instance
     */
    public static FileActionResult file(String path) {

        FileActionResult result = FILE_NOT_FOUND;

        // Try to call another LDPE instance
        try {
            final java.io.File file = new java.io.File(path);
            if (path != null && file.exists() && file.isFile()) {
                try {
                    final WatchSettingsDirectory wcd = new WatchSettingsDirectory(Paths.get(WorkbenchManager.SETTINGS_GZ).toAbsolutePath().getParent(), Paths.get(path));
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
            new Thread(() -> {
                try {
                    new WatchSettingsDirectory(Paths.get(WorkbenchManager.SETTINGS_GZ).toAbsolutePath().getParent()).waitForCall();
                } catch (IOException ioe) {
                    NLogger.error(TryToOpen.class, ioe);
                }
            }).start();
        } else {
            NLogger.error(TryToOpen.class, "File was delegated to another LDPE instance."); //$NON-NLS-1$
        }

        // Return FILE_NOT_FOUND when there was no file to open
        // it returns DELEGATED_TO_ANOTHER_INSTANCE if there is another LDPE instance which will open it
        // it returns WILL_OPEN_FILE if this instance is going to open the file
        return result;
    }

    public static String getFileToOpen() {
        final Path path = WatchSettingsDirectory.getFileToOpen();
        if (path != null) {
            return path.toString();
        }
        return null;
    }

    public static DatFile getDatFileToOpen() {
        if (datFileToOpen == null) {
            final Editor3DWindow win = Editor3DWindow.getWindow();
            datFileToOpen = win.openDatFile(OpenInWhat.EDITOR_TEXT, getFileToOpen(), false);
            if (datFileToOpen == null) {
                NLogger.error(TryToOpen.class, "Could not open file " + getFileName()); //$NON-NLS-1$
                datFileToOpen = View.DUMMY_DATFILE;
            }
        }
        return datFileToOpen;
    }

    private static String getFileName() {
        final Path path = WatchSettingsDirectory.getFileToOpen();
        if (path != null) {
            Path fileName = path.getFileName();
            if (fileName != null) {
                return fileName.toString();
            }
        }
        return null;
    }
}
