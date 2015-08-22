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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * This is the standard {@code BufferedReader} for reading LDraw files
 *
 * @author nils
 *
 */
public class ZipReader {

    /** The {@code BufferedReader} instance [NOT PUBLIC YET] */
    private final DataInputStream myReader;

    /**
     * Creates the standard {@code BufferedReader} for reading LDraw files
     *
     * @param fileName
     *            file to read from
     * @throws IOException
     */
    public ZipReader(String fileName) throws IOException {
        myReader = new DataInputStream(new GZIPInputStream(new FileInputStream(fileName)));
    }

    /**
     * Reads a line of text. A line is considered to be terminated by any one of
     * a line feed ('\n'), a carriage return ('\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return A String containing the contents of the line, not including any
     *         line-termination characters, or null if the end of the stream has
     *         been reached
     * @throws LDParsingException
     */
    public String readLine() throws LDParsingException {
        String line = ""; //$NON-NLS-1$
        try {
            line = myReader.readUTF();
        } catch (IOException e) {
            throw new LDParsingException(e);
        }
        return line;
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     * Closing a previously closed stream has no effect.
     */
    public void close() throws LDParsingException {
        try {
            myReader.close();
        } catch (IOException e) {
            throw new LDParsingException(e);
        }
    }
}
