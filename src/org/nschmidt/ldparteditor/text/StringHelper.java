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


public enum StringHelper {

    INSTANCE;

    public static int countOccurences(final String findStr, final String str) {
        int lastIndex = 0;
        int count = 0;
        lastIndex = str.indexOf(findStr, lastIndex);
        while (lastIndex != -1) {
            count++;
            lastIndex = str.indexOf(findStr, lastIndex + findStr.length());
        }
        return count;
    }

    public static int getIndexFromWhitespaces(final String str, int lastIndex) {
        int count = -1;
        boolean hit = false;
        lastIndex = Math.min(lastIndex, str.length());
        for (int i = 0; i < lastIndex; i++) {
            char c = str.charAt(i);
            if (Character.isWhitespace(c)) {
                hit = false;
            } else if (!hit) {
                hit = true;
                count++;
            }
        }
        return count;
    }

    public static boolean isNotBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static final String LD = String.format("%n"); //$NON-NLS-1$

    public static String getLineDelimiter() {
        return LD;
    }
}
