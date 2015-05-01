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
package org.nschmidt.ldparteditor.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.nschmidt.ldparteditor.helpers.Version;

/**
 * Provides a very simple logger
 *
 * @author nils
 *
 */
// Nothing more to do here..
public enum NLogger {
    INSTANCE;

    // TODO _Set debug variable to false on release!
    public final static boolean DEBUG = false;
    /**
     * The error counter. If it reaches 100 within one session, no more caught
     * errors will be evaluated.
     */
    private static int error_Count = 0;
    /** A flag which indicates, if the logger writes to "error_log2.txt" */
    private static boolean writeInNewFile;
    /** The print stream for errors */
    private static PrintStream errorStream = null;

    /**
     * Initializes the logger.
     */
    public static void init() {
        try {
            if (!DEBUG) {
                File log = new File("error_log.txt"); //$NON-NLS-1$
                if (log.length() > 100000) {
                    log.delete();
                }
                errorStream = new PrintStream(new FileOutputStream("error_log.txt", true)); //$NON-NLS-1$
                System.setErr(errorStream);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[LDPartEditor "); //$NON-NLS-1$
            sb.append(DEBUG ? "DEBUG " : "RELEASE "); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("Started on "); //$NON-NLS-1$
            sb.append(new java.util.Date());
            System.err.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs a exception within the debug mode. It is highly recommended that
     * these method will be substituted by Logger.error(Class<?> clazz,
     * Throwable t) after testing. The message will not be displayed, if
     * LOG_DEBUG is false.
     *
     * @param clazz
     *            The class which triggers this method
     * @param t
     *            The throwable exception
     */
    public static void debug(Class<?> clazz, Throwable t) {
        if (DEBUG) {
            debug_sync(clazz, t);
        }
    }

    /**
     * Logs a message within the debug mode. The message will not be displayed,
     * if LOG_DEBUG is false.
     *
     * @param clazz
     *            The class which triggers this method
     * @param message
     *            The message to display
     */
    public static void debug(Class<?> clazz, String message) {
        if (DEBUG) {
            debug_sync(clazz, message);
        }
    }

    public static void debug(Class<?> clazz, float value) {
        if (DEBUG) {
            debug_sync(clazz, Float.toString(value));
        }
    }

    public static void debug(Class<?> clazz, double value) {
        if (DEBUG) {
            debug_sync(clazz, Double.toString(value));
        }
    }

    public static void debug(Class<?> clazz, int value) {
        if (DEBUG) {
            debug_sync(clazz, Integer.toString(value));
        }
    }

    public static void debugPressAnyKey() {
        if (DEBUG) {
            try {
                System.in.read();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Synchronized debug method call to write the stacktrace
     *
     * @param clazz
     *            The class which triggers this method
     * @param t
     *            The throwable exception
     */
    private static synchronized void debug_sync(Class<?> clazz, Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append("[DEBUG "); //$NON-NLS-1$
        sb.append(Version.getVersion());
        sb.append("] @"); //$NON-NLS-1$
        sb.append(clazz.getName());
        System.out.println(sb.toString());
        t.printStackTrace(System.out);
    }

    /**
     * Synchronized debug method call to write the message
     *
     * @param clazz
     *            The class which triggers this method
     * @param message
     *            The message to display
     */
    private static synchronized void debug_sync(Class<?> clazz, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[DEBUG "); //$NON-NLS-1$
        sb.append(Version.getVersion());
        sb.append("] @"); //$NON-NLS-1$
        sb.append(clazz.getName());
        sb.append("  "); //$NON-NLS-1$
        sb.append(message);
        System.out.println(sb.toString());
    }

    /**
     * Logs an error to System.err
     */
    public static synchronized void error(Class<?> clazz, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("[DEBUG "); //$NON-NLS-1$
        sb.append(Version.getVersion());
        sb.append("] @"); //$NON-NLS-1$
        sb.append(clazz.getName());
        sb.append("  "); //$NON-NLS-1$
        sb.append(message);
        System.err.println(sb.toString());
    }

    /**
     * Logs a error to the flatfiles {@code "error_log.txt"} and
     * {@code "error_log2.txt"} Each file is limited to 100KB file size (~4000
     * lines)
     *
     * @param clazz
     *            The class which triggers this method
     * @param t
     *            The throwable exception
     */
    public static synchronized void error(Class<?> clazz, Throwable t) {
        // Log only 100 errors per session.. ;-)
        if (error_Count < 100) {
            error_Count++;
            FileWriter fw = null;
            if (!writeInNewFile) {
                // Check if "error_log1.txt" is greater than 100KB
                File log1 = new File("error_log.txt"); //$NON-NLS-1$
                writeInNewFile = log1.length() > 100000;
            }
            if (writeInNewFile) {
                // Write in the new "error_log2.txt" file
                try {
                    fw = new FileWriter("error_log2.txt", true); //$NON-NLS-1$
                    StringBuilder sb = new StringBuilder();
                    sb.append("[ERROR "); //$NON-NLS-1$
                    sb.append(Version.getVersion());
                    sb.append("] @"); //$NON-NLS-1$
                    sb.append(clazz.getName());
                    sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
                    sb.append(getStackTrace(t));
                    sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
                    fw.write(sb.toString());
                    fw.flush();
                } catch (IOException ex) {
                    System.err.println("[ERROR] Fatal logging error, caused by java.io.IOException."); //$NON-NLS-1$
                } finally {
                    if (fw != null)
                        try {
                            fw.close();
                        } catch (IOException ex) {
                        }
                }
                try {
                    // Check if "error_log2.txt" is greater than 100KB
                    File log2 = new File("error_log2.txt"); //$NON-NLS-1$
                    if (log2.length() > 100000) {
                        // if so, delete "error_log.txt",
                        File log1 = new File("error_log.txt"); //$NON-NLS-1$
                        log1.delete();
                        // rename "error_log2.txt" to "error_log1.txt"
                        log2.renameTo(log1);
                    }
                } catch (SecurityException ex) {
                    System.err.println("[ERROR] Fatal logging error, caused by java.io.SecurityException. \n You have not enough rights to manipulate files within the application folder."); //$NON-NLS-1$
                }
            } else {
                // Write in the old "error_log1.txt" file
                try {
                    fw = new FileWriter("error_log.txt", true); //$NON-NLS-1$
                    StringBuilder sb = new StringBuilder();
                    sb.append("[ERROR "); //$NON-NLS-1$
                    sb.append(Version.getVersion());
                    sb.append("] @"); //$NON-NLS-1$
                    sb.append(clazz.getName());
                    sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
                    sb.append(getStackTrace(t));
                    sb.append(System.getProperty("line.separator")); //$NON-NLS-1$
                    fw.write(sb.toString());
                    fw.flush();
                } catch (IOException ex) {
                    System.err.println("[ERROR] Fatal logging error, caused by java.io.IOException."); //$NON-NLS-1$
                } finally {
                    if (fw != null)
                        try {
                            fw.close();
                        } catch (IOException ex) {
                        }
                }
            }
        }
    }

    /**
     * Creates and returns a {@link java.lang.String} from {@code t} stacktrace
     *
     * @param t
     *            Throwable whose stack trace is required
     * @return String representing the stack trace of the exception
     */
    private static synchronized String getStackTrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        t.printStackTrace(printWriter);
        printWriter.flush();
        stringWriter.flush();
        return stringWriter.toString();
    }

    /**
     * Flushes the error stream
     */
    public static void flushErrorStream() {
        if (errorStream != null) {
            System.err.flush();
            System.err.close();
        }
    }

}
