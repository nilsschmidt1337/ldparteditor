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

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * This is the standard {@code PrintWriter} for writing LDraw files
 *
 * @author nils
 *
 */
public class UTF8PrintWriter implements AutoCloseable {

    /** The {@code PrintWriter} instance [NOT PUBLIC YET] */
    private final PrintWriter myWriter;

    /**
     * Creates the standard {@code PrintWriter} for writing LDraw files
     *
     * @param fileName
     *            file to write in
     */
    public UTF8PrintWriter(String fileName) throws IOException {
        myWriter = new PrintWriter(fileName, StandardCharsets.UTF_8.toString());
    }

    /**
     * Prints a String and then terminates the line with the standard
     * DOS/Windows line termination. This method behaves as though it invokes
     * print(String) and then println().
     *
     * @param x
     *            the String value to be printed
     */
    public void println(String x) {
        myWriter.print(x);
        myWriter.print("\r\n"); //$NON-NLS-1$ Its very important to use the windows line termination here (see documentation!)
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     * Closing a previously closed stream has no effect.
     */
    @Override
    public void close() {
        myWriter.close();
    }

    /**
     * Flushes the stream.
     */
    public void flush() {
        myWriter.flush();
    }
}
