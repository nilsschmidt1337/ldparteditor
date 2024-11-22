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
import java.util.Locale;

import org.nschmidt.ldparteditor.logger.NLogger;

public enum Win32LnkParser {
    INSTANCE;

    private static final String BACKSLASH = "\\"; //$NON-NLS-1$
    private static final String CURRENT_DIR_PREFIX = ".\\"; //$NON-NLS-1$
    private static final String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * Parses and resolves Windows *.lnk shortcut files
     * @param lnkPath the shortcut file path.
     * @return the actual file path.
     */
    public static String resolveLnkShortcut(String lnkPath) {
        if (!lnkPath.toLowerCase(Locale.ENGLISH).endsWith(".lnk")) { //$NON-NLS-1$
            return lnkPath;
        }

        final File lnkFile = new File(lnkPath);
        NLogger.debug(Win32LnkParser.class, "Resolving link of: {0}", lnkFile.getAbsolutePath()); //$NON-NLS-1$

        try (DataInputStream is = new DataInputStream(new FileInputStream(lnkFile))) {

            // Read header size
            final int headerSize = Integer.reverseBytes(is.readInt());
            NLogger.debug(Win32LnkParser.class, "HeaderSize: {0}", Integer.toHexString(headerSize)); //$NON-NLS-1$

            // Read the header
            final int[] header = readNBytes(is, headerSize - 4);

            // CLSID
            final StringBuilder sb = new StringBuilder();
            sb.append("CLSID: "); //$NON-NLS-1$
            sb.append(Integer.toHexString(header[3]));
            sb.append(Integer.toHexString(header[2]));
            sb.append(Integer.toHexString(header[1]));
            sb.append(Integer.toHexString(header[0]));
            sb.append('-');
            sb.append(Integer.toHexString(header[5]));
            sb.append(Integer.toHexString(header[4]));
            sb.append('-');
            sb.append(Integer.toHexString(header[7]));
            sb.append(Integer.toHexString(header[6]));
            sb.append('-');
            sb.append(Integer.toHexString(header[8]));
            sb.append(Integer.toHexString(header[9]));
            sb.append('-');
            sb.append(Integer.toHexString(header[10]));
            sb.append(Integer.toHexString(header[11]));
            sb.append(Integer.toHexString(header[12]));
            sb.append(Integer.toHexString(header[13]));
            sb.append(Integer.toHexString(header[14]));
            sb.append(Integer.toHexString(header[15]));
            // should be: "CLSID: 02141-00-00-c00-0000046"
            NLogger.debug(Win32LnkParser.class, sb.toString());

            // LinkFlags
            final boolean hasLinkTargetIDList = flag(header[16], 0);
            final boolean hasLinkInfo = flag(header[16], 1);
            final boolean hasName = flag(header[16], 2);
            final boolean hasRelativePath = flag(header[16], 3);
            final boolean hasWorkingDir = flag(header[16], 4);
            final boolean isUnicode = flag(header[16], 7);

            NLogger.debug(Win32LnkParser.class, "HasLinkTargetIDList  : {0}", hasLinkTargetIDList); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasLinkInfo          : {0}", hasLinkInfo); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasName              : {0}", hasName); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasRelativePath      : {0}", hasRelativePath); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasWorkingDir        : {0}", hasWorkingDir); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasArguments         : {0}", flag(header[16], 5)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasIconLocation      : {0}", flag(header[16], 6)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "IsUnicode            : {0}\n", isUnicode); //$NON-NLS-1$

            NLogger.debug(Win32LnkParser.class, "ForceNoLinkInfo      : {0}", flag(header[17], 0)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasExpString         : {0}", flag(header[17], 1)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "RunInSeparateProcess : {0}", flag(header[17], 2)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "Unused1              : {0}", flag(header[17], 3)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasDarwinID          : {0}", flag(header[17], 4)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "RunAsUser            : {0}", flag(header[17], 5)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "HasExpIcon           : {0}", flag(header[17], 6)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "NoPidlAlias          : {0}\n", flag(header[17], 7)); //$NON-NLS-1$

            NLogger.debug(Win32LnkParser.class, "Unused2              : {0}", flag(header[18], 0)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "RunWithShimLayer     : {0}", flag(header[18], 1)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "ForceNoLinkTrack     : {0}", flag(header[18], 2)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "EnableTargetMetadata : {0}", flag(header[18], 3)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "DisableLinkPathTracking     : {0}", flag(header[18], 4)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "DisableKnownFolderTracking  : {0}", flag(header[18], 5)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "DisableKnownFolderAlias     : {0}", flag(header[18], 6)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "AllowLinkToLink             : {0}\n", flag(header[18], 7)); //$NON-NLS-1$

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
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_NORMAL              : {0}\n", flag(header[20], 7)); //$NON-NLS-1$

            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_TEMPORARY           : {0}", flag(header[21], 0)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_SPARSE_FILE         : {0}", flag(header[21], 1)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_REPARSE_POINT       : {0}", flag(header[21], 2)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_COMPRESSED          : {0}", flag(header[21], 3)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_OFFLINE             : {0}", flag(header[21], 4)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_NOT_CONTENT_INDEXED : {0}", flag(header[21], 5)); //$NON-NLS-1$
            NLogger.debug(Win32LnkParser.class, "FILE_ATTRIBUTE_ENCRYPTED           : {0}\n", flag(header[21], 6)); //$NON-NLS-1$

            if (!isUnicode) {
                NLogger.debug(Win32LnkParser.class, "File contains no unicode encoded paths."); //$NON-NLS-1$
                return lnkPath;
            }

            if (hasLinkTargetIDList) {
                final long targetListSize = readUnsignedShort(is);
                NLogger.debug(Win32LnkParser.class, "IDListSize: {0}", targetListSize); //$NON-NLS-1$

                // Read and skip the target info
                is.readNBytes((int) Math.max(targetListSize, 0));
            }

            if (hasLinkInfo) {
                final long linkInfoSize = readUnsignedInt(is);
                NLogger.debug(Win32LnkParser.class, "LinkInfoSize: {0}", linkInfoSize); //$NON-NLS-1$

                // Read and skip the link info (-4 because the size was already part of the link info section)
                is.readNBytes((int) Math.max(linkInfoSize-4, 0));
            }


            // StringData
            if (hasName) {
                final String name = readString(is);
                NLogger.debug(Win32LnkParser.class, "name?         :" + name); //$NON-NLS-1$
            }

            // workingDir + relative path (thats the info we want!)
            final String relativePathWithoutCurrentFolder;
            if (hasRelativePath) {
                final String relativePath = readString(is);
                NLogger.debug(Win32LnkParser.class, "relativePath? :" + relativePath); //$NON-NLS-1$

                if (relativePath.startsWith(CURRENT_DIR_PREFIX)) {
                    relativePathWithoutCurrentFolder = relativePath.substring(2);
                } else {
                    relativePathWithoutCurrentFolder = relativePath;
                }
            } else {
                relativePathWithoutCurrentFolder = EMPTY_STRING;
            }

            final String resultingPath;
            if (hasWorkingDir) {
                final String workingDir = readString(is);
                NLogger.debug(Win32LnkParser.class, "workingDir?   :" + workingDir); //$NON-NLS-1$

                if (workingDir.endsWith(BACKSLASH)) {
                    resultingPath = workingDir + relativePathWithoutCurrentFolder;
                } else {
                    resultingPath = workingDir + BACKSLASH + relativePathWithoutCurrentFolder;
                }
            } else {
                resultingPath = EMPTY_STRING;
            }

            NLogger.debug(Win32LnkParser.class, "resultingPath?:" + resultingPath); //$NON-NLS-1$

            return resultingPath;
        } catch (IOException ex) {
            NLogger.debug(Win32LnkParser.class, ex);
        }

        return lnkPath;
    }

    private static String readString(InputStream is) throws IOException {
        final StringBuilder sb2 = new StringBuilder();
        final long charCount = readUnsignedShort(is);
        for (int i = 0; i < charCount; i++) {
            sb2.append((char) readUnsignedShort(is));
        }

        // This is unicode or something else?
        return sb2.toString();
    }

    private static boolean flag(final int b, int bitIndex) {
        return (b & (1 << bitIndex)) > 0;
    }

    private static long readUnsignedInt(InputStream is) throws IOException {
        final long bLowest = is.read();
        final long bLower = is.read();
        final long bUpper = is.read();
        final long bUpmost = is.read();
        // this is lower endian
        return bLowest | (bLower << 8) | (bUpper << 16) | (bUpmost << 24);
    }

    private static long readUnsignedShort(InputStream is) throws IOException {
        final long bLower = is.read();
        final long bUpper = is.read();
        // this is lower endian
        return bLower | (bUpper << 8);
    }

    private static int[] readNBytes(InputStream is, int n) throws IOException {
        final int[] result = new int[n];
        for (int i = 0; i < n; i++) {
            result[i] = is.read();
        }

        return result;
    }
}
