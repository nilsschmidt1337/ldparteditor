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
import java.util.regex.Pattern;

import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.workbench.UserSettingState;

public enum Stl2Dat {
    INSTANCE;
    
    private static final Pattern WHITESPACE = Pattern.compile("\\s+"); //$NON-NLS-1$
    
    public static String convertStlToDatFile(String path, UserSettingState userSetting) {
        final StringBuilder result = new StringBuilder();
        appendPartHeader(result, userSetting);
        
        boolean readBinaryStl = false;
        
        try (UTF8BufferedReader reader = new UTF8BufferedReader(path)) {
            String firstLine = reader.readLine();
            if (firstLine.startsWith("solid ")) { //$NON-NLS-1$
                readAsciiStlFile(result, reader);
            } else {
                readBinaryStl = true;
            }
        } catch (IOException ex) {
            NLogger.debug(Stl2Dat.class, ex);
            readBinaryStl = true;
        }
        
        if (readBinaryStl) {
            readBinaryStlFile(result, path);
        }
        
        return result.toString();
    }

    private static void appendPartHeader(StringBuilder result, UserSettingState userSetting) {
        result.append("0 STL-Import\r\n"); //$NON-NLS-1$
        result.append("0 Name: stl.dat\r\n"); //$NON-NLS-1$
        String ldrawName = userSetting.getLdrawUserName();
        if (ldrawName == null || ldrawName.isEmpty()) {
            result.append("0 Author: " + userSetting.getRealUserName()+ "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            result.append("0 Author: " + userSetting.getRealUserName() + " [" + userSetting.getLdrawUserName() + "]\r\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        
        result.append("0 !LDRAW_ORG Unofficial_\r\n"); //$NON-NLS-1$
        String license = userSetting.getLicense();
        if (license == null || license.isEmpty()) {
            result.append("0 !LICENSE Licensed under CC BY 4.0 : see CAreadme.txt\r\n"); //$NON-NLS-1$
        } else {
            result.append(license);
            result.append("\r\n"); //$NON-NLS-1$
        }
        
        result.append("\r\n"); //$NON-NLS-1$
        result.append("0 BFC CERTIFY CCW\r\n"); //$NON-NLS-1$
        result.append("\r\n"); //$NON-NLS-1$
    }
    
    private static void readAsciiStlFile(StringBuilder result, UTF8BufferedReader reader) throws LDParsingException {
        String line;
        
        int counter = 0;
        
        StringBuilder lineSb = new StringBuilder();
        lineSb.append("3 16 "); //$NON-NLS-1$
        while ((line = reader.readLine()) != null) {
            counter++;
            line = line.trim();
            if (line.startsWith("endsolid")) break; //$NON-NLS-1$
            
            if (counter == 3 || counter == 4) {
                String[] lineSegments = WHITESPACE.split(line);
                lineSb.append(lineSegments[1] + " " + lineSegments[2] + " " + lineSegments[3] + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else if (counter == 5) {
                String[] lineSegments = WHITESPACE.split(line);
                lineSb.append(lineSegments[1] + " " + lineSegments[2] + " " + lineSegments[3]); //$NON-NLS-1$ //$NON-NLS-2$
            } else if (counter == 7) {
                result.append(lineSb.toString());
                result.append("\r\n"); //$NON-NLS-1$
                
                lineSb.setLength(0);
                lineSb.append("3 16 "); //$NON-NLS-1$
                counter = 0;
            }
        }
    }
    
    private static void readBinaryStlFile(StringBuilder result, String path) {
        try (DataInputStream is = new DataInputStream(new FileInputStream(path))) {
            // Read header
            is.readNBytes(80);
            // Read number of facets in file
            is.readNBytes(4);
            // Read bytes for normal (ignored)
            while (is.readNBytes(12).length != 0) {
                // Read triangle
                result.append("3 16 "); //$NON-NLS-1$
                result.append(readFloat(is));
                result.append(' ');
                result.append(readFloat(is));
                result.append(' ');
                result.append(readFloat(is));
                
                result.append(' ');
                result.append(readFloat(is));
                result.append(' ');
                result.append(readFloat(is));
                result.append(' ');
                result.append(readFloat(is));
                
                result.append(' ');
                result.append(readFloat(is));
                result.append(' ');
                result.append(readFloat(is));
                result.append(' ');
                result.append(readFloat(is));
                result.append('\r');
                result.append('\n');
                // Read attribute byte count (usually zero, ignored)
                is.readNBytes(2);
            }
        } catch (IOException ex) {
            NLogger.debug(Stl2Dat.class, ex);
            result.append("0 // "); //$NON-NLS-1$
            result.append(ex.getMessage());
            result.append('\r');
            result.append('\n');
        }
    }

    private static float readFloat(DataInputStream is) throws IOException {
        final byte[] bytes = is.readNBytes(4);
        if (bytes.length != 4) return 0f;
        
        int value = bytes[3] & 0xFF;
        value = (value << 8) + (bytes[2] & 0xFF);
        value = (value << 8) + (bytes[1] & 0xFF);
        value = (value << 8) + (bytes[0] & 0xFF);
        
        return Float.intBitsToFloat(value);
    }
}
