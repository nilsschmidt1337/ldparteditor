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
package org.nschmidt.ldparteditor.win32appdata;

import java.io.File;

import org.eclipse.swt.SWT;
import org.nschmidt.ldparteditor.helper.FileHelper;

public enum AppData {
    INSTANCE;

    private static String path = null;

    /**
     * Windows only: Gets the path to LDPE's AppData folder
     *
     * @return {drive}\Documents and Settings\{user}\Application Data\LDPartEditor\
     */
    public static String getPath() {
        if (path != null) {
            return path;
        }

        if ("win32".equals(SWT.getPlatform())) { //$NON-NLS-1$
            try {
                path = System.getenv("APPDATA"); //$NON-NLS-1$
                if (path == null) {
                    path = ""; //$NON-NLS-1$
                } else {
                    if (!path.endsWith(File.separator)) {
                        path = path + File.separator;
                    }

                    if (FileHelper.canReadFromPath(path) && FileHelper.canWriteToPath(path)) {
                        path = path + "LDPartEditor" + File.separator; //$NON-NLS-1$
                        final File settingsFolder = new File(path);
                        if (!settingsFolder.exists()) {
                            // Try to create the directory
                            if (!settingsFolder.mkdir()) {
                                path = ""; //$NON-NLS-1$
                            }
                        } else if (!FileHelper.canReadFromPath(path) || !FileHelper.canWriteToPath(path)) {
                            path = ""; //$NON-NLS-1$
                        }
                    } else {
                        path = ""; //$NON-NLS-1$
                    }
                }
            } catch (SecurityException se) {
                // Do not log this, since the logger needs the AppData path.
                path = ""; //$NON-NLS-1$
            }
        } else {
            path = ""; //$NON-NLS-1$
        }

        return path;
    }
}
