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
import java.io.InputStream;

import org.nschmidt.ldparteditor.logger.NLogger;

public enum Win32LnkParser {
    INSTANCE;

    /**
     * Parses and resolves Windows *.lnk shortcut files
     * @param lnkFile the shortcut file.
     * @return the actual file.
     */
    public static File resolveLnkShortcut(File lnkFile) {
        NLogger.debug(Win32LnkParser.class, "Resolving link of: {0}", lnkFile.getAbsolutePath()); //$NON-NLS-1$

        try (DataInputStream is = new DataInputStream(new FileInputStream(lnkFile))) {
            // FIXME Needs implementation!

            // Read header size
            final int headerSize = Integer.reverseBytes(is.readInt());
            NLogger.debug(Win32LnkParser.class, "HeaderSize: {0}", Integer.toHexString(headerSize)); //$NON-NLS-1$

            // Read the header
            final byte[] header = is.readNBytes(headerSize - 4);

            // CLSID
            final StringBuilder sb = new StringBuilder();
            sb.append("CLSID: "); //$NON-NLS-1$
            sb.append(Integer.toHexString(byteToInt(header[3])));
            sb.append(Integer.toHexString(byteToInt(header[2])));
            sb.append(Integer.toHexString(byteToInt(header[1])));
            sb.append(Integer.toHexString(byteToInt(header[0])));
            sb.append("-"); //$NON-NLS-1$
            sb.append(Integer.toHexString(byteToInt(header[5])));
            sb.append(Integer.toHexString(byteToInt(header[4])));
            sb.append("-"); //$NON-NLS-1$
            sb.append(Integer.toHexString(byteToInt(header[7])));
            sb.append(Integer.toHexString(byteToInt(header[6])));
            sb.append("-"); //$NON-NLS-1$
            sb.append(Integer.toHexString(byteToInt(header[8])));
            sb.append(Integer.toHexString(byteToInt(header[9])));
            sb.append("-"); //$NON-NLS-1$
            sb.append(Integer.toHexString(byteToInt(header[10])));
            sb.append(Integer.toHexString(byteToInt(header[11])));
            sb.append(Integer.toHexString(byteToInt(header[12])));
            sb.append(Integer.toHexString(byteToInt(header[13])));
            sb.append(Integer.toHexString(byteToInt(header[14])));
            sb.append(Integer.toHexString(byteToInt(header[15])));
            NLogger.debug(Win32LnkParser.class, sb.toString());

            // LinkFlags
            final boolean hasLinkTargetIDList = flag(header[16], 0);
            final boolean hasLinkInfo = flag(header[16], 1);
            final boolean hasName = flag(header[16], 2);

            NLogger.debug(Win32LnkParser.class, "HasLinkTargetIDList  : {0}", hasLinkTargetIDList); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasLinkInfo          : {0}", hasLinkInfo); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasName              : {0}", hasName); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasRelativePath      : {0}", flag(header[16], 3)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasWorkingDir        : {0}", flag(header[16], 4)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasArguments         : {0}", flag(header[16], 5)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasIconLocation      : {0}", flag(header[16], 6)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "IsUnicode            : {0}", flag(header[16], 7)); //$NON-NLS-1$

            NLogger.debug(Win32LnkParser.class, "ForceNoLinkInfo      : {0}", flag(header[17], 0)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasExpString         : {0}", flag(header[17], 1)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "RunInSeparateProcess : {0}", flag(header[17], 2)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "Unused1              : {0}", flag(header[17], 3)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasDarwinID          : {0}", flag(header[17], 4)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "RunAsUser            : {0}", flag(header[17], 5)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasExpIcon           : {0}", flag(header[17], 6)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "NoPidlAlias          : {0}", flag(header[17], 7)); //$NON-NLS-1$

            NLogger.debug(Win32LnkParser.class, "Unused2              : {0}", flag(header[18], 0)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "RunWithShimLayer     : {0}", flag(header[18], 1)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "ForceNoLinkTrack     : {0}", flag(header[18], 2)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "EnableTargetMetadata : {0}", flag(header[18], 3)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "DisableLinkPathTracking     : {0}", flag(header[18], 4)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "DisableKnownFolderTracking  : {0}", flag(header[18], 5)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "DisableKnownFolderAlias     : {0}", flag(header[18], 6)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "AllowLinkToLink             : {0}", flag(header[18], 7)); //$NON-NLS-1$

            NLogger.debug(Win32LnkParser.class, "UnaliasOnSave               : {0}", flag(header[19], 0)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "PreferEnvironmentPath       : {0}", flag(header[19], 1)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "KeepLocalIDListForUNCTarget : {0}", flag(header[19], 2)); //$NON-NLS-1$

            // FileAttributesFlags
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_READONLY            : {0}", flag(header[20], 0)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_HIDDEN              : {0}", flag(header[20], 1)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_SYSTEM              : {0}", flag(header[20], 2)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "Reserved1                          : {0}", flag(header[20], 3)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_DIRECTORY           : {0}", flag(header[20], 4)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_ARCHIVE             : {0}", flag(header[20], 5)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "Reserved2                          : {0}", flag(header[20], 6)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_NORMAL              : {0}", flag(header[20], 7)); //$NON-NLS-1$

            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_TEMPORARY           : {0}", flag(header[21], 0)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_SPARSE_FILE         : {0}", flag(header[21], 1)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_REPARSE_POINT       : {0}", flag(header[21], 2)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_COMPRESSED          : {0}", flag(header[21], 3)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_OFFLINE             : {0}", flag(header[21], 4)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_NOT_CONTENT_INDEXED : {0}", flag(header[21], 5)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_ENCRYPTED           : {0}", flag(header[21], 6)); //$NON-NLS-1$

            if (hasLinkTargetIDList) {
                final long targetListSize = readUnsignedShort(is);
                NLogger.debug(Win32LnkParser.class, "IDListSize: {0}", targetListSize); //$NON-NLS-1$

                // Read the target info
                final byte[] targetListInfo = is.readNBytes((int) Math.max(targetListSize, 0));

            }

            if (hasLinkInfo) {
                final long linkInfoSize = readUnsignedShort(is);
                NLogger.debug(Win32LnkParser.class, "LinkInfoSize: {0}", linkInfoSize); //$NON-NLS-1$

                // Read the link info
                final byte[] linkInfo = is.readNBytes((int) Math.max(linkInfoSize, 0));
            }


            // StringData
            // name + relative path (thats the info we want!)
            if (hasName) {
                final StringBuilder sb2 = new StringBuilder();
                final long charCount = readUnsignedShort(is);
                for (int i = 0; i < charCount; i++) {
                    sb2.append((char) is.readByte());
                }

                NLogger.error(Win32LnkParser.class, "Name? :" + sb2.toString()); //$NON-NLS-1$
            }


            final StringBuilder sb3 = new StringBuilder();

            while (is.available() > 0) {
                sb3.append((char) is.readByte());
            }

            NLogger.error(Win32LnkParser.class, sb3.toString());

        } catch (IOException ex) {
            NLogger.debug(Win32LnkParser.class, ex);
        }

        return lnkFile;
    }

    private static int byteToInt(final byte b) {
        return b < 0 ?  128 - b : b;
    }

    private static boolean flag(final byte b, int bitIndex) {
        return (byteToInt(b) & (1 << (7 - bitIndex))) > 0;
    }

    private static long readUnsignedShort(InputStream is) throws IOException {
        final long bLower = is.read();
        final long bUpper = is.read();
        // this is lower endian
        return bLower | (bUpper << 8);
    }
}
