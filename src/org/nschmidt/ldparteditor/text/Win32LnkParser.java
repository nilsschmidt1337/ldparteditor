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
package org.nschmidt.ldparteditor.text;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.nschmidt.ldparteditor.logger.NLogger;

public enum Win32LnkParser {
    INSTANCE;

    /**
     * Parses and resolves Windows *.lnk shortcut files
     * @param lnkFile the shortcut file.
     * @return the actual file.
     */
    public static File resolveLnkShortcut(File lnkFile) {
        try (DataInputStream is = new DataInputStream(new FileInputStream(lnkFile))) {
            // FIXME Needs implementation!

            // Read header size
            final int headerSize = Integer.reverseBytes(is.readInt());
            System.out.println(Integer.toHexString(headerSize));

            // Read and skip the header
            is.readNBytes(headerSize);

            // Read and skip 4 bytes of LinkFlags
            is.readInt();

            // Read and skip 4 bytes of FileAttributesFlags
            is.readInt();

            // Read and skip 4 bytes of HotKeyFlags
            is.readInt();

            // Read and skip the LinkTargetIDList
            final int idListSize = is.readUnsignedShort();
            is.readNBytes(idListSize);

        } catch (IOException ex) {
            NLogger.debug(Win32LnkParser.class, ex);
        }

        return lnkFile;
    }
}
