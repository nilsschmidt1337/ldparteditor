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
import java.nio.file.Files;
import java.text.MessageFormat;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.nschmidt.ldparteditor.helper.Version;
import org.nschmidt.ldparteditor.win32appdata.AppData;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Provides a very simple logger
 *
 * @author nils
 *
 */
// Nothing more to do here..
public enum NLogger {
    INSTANCE;

    public static boolean debugging = false;

    /**
     * The error counter. If it reaches 100 within one session, no more caught
     * errors will be evaluated.
     */
    private static int errorCount = 0;

    /** A flag which indicates, if the logger writes to ERROR_LOG2 */
    private static boolean writeInNewFile;

    /** The print stream for errors */
    private static PrintStream errorStream = null;

    /** path to log-file 1 */
    public static final String ERROR_LOG = AppData.getPath() + "error_log.txt"; //$NON-NLS-1$

    /** path to log-file 2 */
    public static final String ERROR_LOG2 = AppData.getPath() + "error_log2.txt"; //$NON-NLS-1$

    /**
     * Initializes the logger.
     */
    public static void init() {
        try {
            if (!debugging) {
                File log = new File(ERROR_LOG);
                if (log.length() <= 100000 || Files.deleteIfExists(log.toPath())) {
                    // Only log to a file when it is small enough or it can be safely deleted when it is too big.
                    errorStream = new PrintStream(new FileOutputStream(ERROR_LOG, true));
                    System.setErr(errorStream);
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("[LDPartEditor "); //$NON-NLS-1$
            sb.append(debugging ? "DEBUG " : "RELEASE "); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append("] Started on "); //$NON-NLS-1$
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
        if (debugging) {
            debugSync(clazz, t);
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
        if (debugging) {
            debugSync(clazz, message);
        }
    }

    /**
     * Logs a message within the debug mode. The message will not be displayed,
     * if LOG_DEBUG is false. The message has parameters "args" separated with
     * a comma
     *
     * @param clazz
     *            The class which triggers this method
     * @param message
     *            The message format string to display (e.g. "Step {0} of {1}")
     * @param args
     *            Parameters separated with a comma
     */
    public static void debug(Class<?> clazz, String message, Object... args) {
        if (debugging) {
            debugSync(clazz, message, args);
        }
    }

    public static void debug(Class<?> clazz, float value) {
        if (debugging) {
            debugSync(clazz, Float.toString(value));
        }
    }

    public static void debug(Class<?> clazz, double value) {
        if (debugging) {
            debugSync(clazz, Double.toString(value));
        }
    }

    public static void debug(Class<?> clazz, int value) {
        if (debugging) {
            debugSync(clazz, Integer.toString(value));
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
    private static synchronized void debugSync(Class<?> clazz, Throwable t) {
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
     * @param args
     *            Parameters separated with a comma
     */
    private static synchronized void debugSync(Class<?> clazz, String message, Object... args) {
        MessageFormat formatter = new MessageFormat("", Locale.ENGLISH); //$NON-NLS-1$
        formatter.applyPattern(message);
        StringBuilder sb = new StringBuilder();
        sb.append("[DEBUG "); //$NON-NLS-1$
        sb.append(Version.getVersion());
        sb.append("] @"); //$NON-NLS-1$
        sb.append(clazz.getName());
        sb.append("  "); //$NON-NLS-1$
        sb.append(formatter.format(args));
        System.out.println(sb.toString());
    }

    /**
     * Synchronized debug method call to write the message
     *
     * @param clazz
     *            The class which triggers this method
     * @param message
     *            The message to display
     */
    private static synchronized void debugSync(Class<?> clazz, String message) {
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
     * Logs the version number to System.err
     */
    public static synchronized void writeVersion() {
        StringBuilder sb = new StringBuilder();
        sb.append("[LDPartEditor "); //$NON-NLS-1$
        sb.append(debugging ? "DEBUG " : "RELEASE "); //$NON-NLS-1$ //$NON-NLS-2$
        sb.append(Version.getVersion());
        sb.append(" @ SWT v"); //$NON-NLS-1$
        sb.append(SWT.getVersion());
        sb.append(" " + WorkbenchManager.getUserSettingState().getOpenGLVersionString()); //$NON-NLS-1$
        sb.append(" PLATFORM "); //$NON-NLS-1$
        sb.append(SWT.getPlatform());
        sb.append("]"); //$NON-NLS-1$
        System.err.println(sb.toString());
    }

    /**
     * Logs a error to the flatfiles {@code ERROR_LOG} and
     * {@code ERROR_LOG2} Each file is limited to 100KB file size (~4000
     * lines)
     *
     * @param clazz
     *            The class which triggers this method
     * @param t
     *            The throwable exception
     */
    public static synchronized void error(Class<?> clazz, Throwable t) {
        // Log only 100 errors per session.. ;-)
        if (errorCount < 100) {
            errorCount++;
            if (!writeInNewFile) {
                // Check if "error_log1.txt" is greater than 100KB
                File log1 = new File(ERROR_LOG);
                writeInNewFile = log1.length() > 100000;
            }
            if (writeInNewFile) {
                // Write in the new ERROR_LOG2 file
                try (FileWriter fw = new FileWriter(ERROR_LOG2, true)) {
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
                }
                try {
                    // Check if ERROR_LOG2 is greater than 100KB
                    File log2 = new File(ERROR_LOG2);
                    if (log2.length() > 100000) {
                        // if so, delete ERROR_LOG,
                        File log1 = new File(ERROR_LOG);
                        // rename ERROR_LOG2 to ERROR_LOG
                        if (!Files.deleteIfExists(log1.toPath()) || !log2.renameTo(log1)) {
                            throw new SecurityException();
                        }
                    }
                } catch (IOException | SecurityException ex) {
                    System.err.println("[ERROR] Fatal logging error, caused by java.io.SecurityException. \n You have not enough rights to manipulate files within the application folder."); //$NON-NLS-1$
                }
            } else {
                // Write in the old "error_log1.txt" file
                try (FileWriter fw = new FileWriter(ERROR_LOG, true)) {
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
    public static synchronized String getStackTrace(Throwable t) {
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

    public static void setDebugging(boolean debug) {
        debugging = debug;
    }

}
