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
package org.nschmidt.ldparteditor.helpers;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.xerces.internal.impl.io.UTF8Reader;

/**
 * A helper enum class for file actions
 *
 * @author nils TODO I redesigned this class to match with the Singleton design
 *         pattern as suggested by Joshua Bloch \n This has to be done for all
 *         other static classes too
 */
public enum FileHelper {
    INSTANCE;
    /**
     * Validates a filename
     *
     * @param file
     *            the filename to validate
     * @return {@code true} if the filename is valid
     */
    public static boolean isFilenameValid(String file) {
        if (file.trim().isEmpty())
            return false;
        Pattern pattern = Pattern.compile("# Match a valid Windows filename (unspecified file system).          \n" + //$NON-NLS-1$
                "^                                # Anchor to start of string.        \n" + //$NON-NLS-1$
                "(?!                              # Assert filename is not: CON, PRN, \n" + //$NON-NLS-1$
                "  (?:                            # AUX, NUL, COM1, COM2, COM3, COM4, \n" + //$NON-NLS-1$
                "    CON|PRN|AUX|NUL|             # COM5, COM6, COM7, COM8, COM9,     \n" + //$NON-NLS-1$
                "    COM[1-9]|LPT[1-9]            # LPT1, LPT2, LPT3, LPT4, LPT5,     \n" + //$NON-NLS-1$
                "  )                              # LPT6, LPT7, LPT8, and LPT9...     \n" + //$NON-NLS-1$
                "  (?:\\.[^.]*)?                  # followed by optional extension    \n" + //$NON-NLS-1$
                "  $                              # and end of string                 \n" + //$NON-NLS-1$
                ")                                # End negative lookahead assertion. \n" + //$NON-NLS-1$
                "[^<>:\"/\\\\|?*\\x00-\\x1F]*     # Zero or more valid filename chars.\n" + //$NON-NLS-1$
                "[^<>:\"/\\\\|?*\\x00-\\x1F\\ .]  # Last char is not a space or dot.  \n" + //$NON-NLS-1$
                "$                                # Anchor to end of string.            ", //$NON-NLS-1$
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.COMMENTS);
        Matcher matcher = pattern.matcher(file);
        boolean isMatch = matcher.matches();
        File f = new File(file);
        try {
            f.getCanonicalPath();
        } catch (Exception e) {
            return false;
        }
        return isMatch;
    }

    /**
     * Validates readability
     *
     * @param path
     *            the file/path to validate
     * @return {@code true} if the application can read from this location
     */
    public static boolean canReadFromPath(String path) {
        File f = new File(path);
        try {
            return f.canRead();
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Validates file writing
     *
     * @param path
     *            the file/path to validate
     * @return {@code true} if the application can write to this location
     */
    public static boolean canWriteToPath(String path) {
        File f = new File(path);
        try {
            return f.canWrite();
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Deletes the content of the directory recursively
     *
     * @param dir
     *            the directory
     */
    public static void deleteDirectory(File dir) {
        try {
            if (!dir.delete() && dir.isDirectory()) {
                for (File f : dir.listFiles()) {
                    deleteDirectory(f);
                }
                dir.delete();
            }
        } catch (SecurityException e) {
        }
    }

    /**
     * Checks the (platform indepedend) existance of a given file
     *
     * @param file
     *            the file to investigate
     * @return {@code null} if the file does not exist
     */
    public static File exist(File file) {
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public static String downloadPartFile(String name) {

        final StringBuilder sb = new StringBuilder();

        UTF8Reader in = null;
        try {
            in = new UTF8Reader(new URL("http://www.ldraw.org/library/unofficial/" + name).openStream()); //$NON-NLS-1$
            int c;
            while ((c = in.read()) != -1) {
                sb.append((char) c);
            }

        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return sb.toString();
    }

}
