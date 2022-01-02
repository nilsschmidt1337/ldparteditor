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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * This is the standard {@code BufferedReader} for reading LDraw files
 *
 * @author nils
 *
 */
public class UTF8BufferedReader implements AutoCloseable {

    /** The {@code BufferedReader} instance [NOT PUBLIC YET] */
    private final BufferedReader myReader;

    /**
     * Creates the standard {@code BufferedReader} for reading LDraw files
     *
     * @param fileName
     *            file to read from
     * @throws FileNotFoundException
     * @throws LDParsingException
     */
    public UTF8BufferedReader(String fileName) throws FileNotFoundException, LDParsingException {
        myReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));
        char[] bom = new char[3];
        try {
            myReader.mark(3);
            if (3 == myReader.read(bom, 0, 3) && (bom[0] != (char) 0xEF || bom[1] != (char) 0xBB || bom[2] != (char) 0xBF)) {
                myReader.reset();
            }
        } catch (IOException e) {
            throw new LDParsingException();
        }
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
        String line;
        try {
            line = myReader.readLine();
        } catch (IOException e) {
            throw new LDParsingException(e);
        }
        return line;
    }

    /**
     * Closes the stream and releases any system resources associated with it.
     * Closing a previously closed stream has no effect.
     */
    @Override
    public void close() throws LDParsingException {
        try {
            myReader.close();
        } catch (IOException e) {
            throw new LDParsingException(e);
        }
    }
}
