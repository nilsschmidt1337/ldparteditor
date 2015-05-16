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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author nils
 *
 */
public enum StringHelper {

    INSTANCE;

    public static final StringBuilder useAgain() { return new StringBuilder(); }
    public static final StringBuilder useAgain2() { return new StringBuilder(); }

    public static int countOccurences(final String findStr, final String str) {
        int lastIndex = 0;
        int count = 0;

        while (lastIndex != -1) {

            lastIndex = str.indexOf(findStr, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += findStr.length();
            }
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
            } else {
                if (!hit) {
                    hit = true;
                    count++;
                }
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
            if (Character.isWhitespace(str.charAt(i)) == false) {
                return true;
            }
        }
        return false;
    }

    private static final String ld = String.format("%n"); //$NON-NLS-1$

    public static String getLineDelimiter() {
        return ld;
    }

    /**
     * Lempel–Ziv–Welch (LZW) Compression for UNICODE Strings
     * @param uncompressed
     * @return
     */
    public static int[] compress(String uncompressed) {
        // Build the dictionary.
        int dictSize = 256;
        Map<String,Integer> dictionary = new HashMap<String,Integer>();
        for (int i = 0; i < 256; i++)
            dictionary.put("" + (char)i, i); //$NON-NLS-1$
        String w = ""; //$NON-NLS-1$
        List<Integer> result = new ArrayList<Integer>();
        for (char c : uncompressed.toCharArray()) {
            String wc = w + c;
            if (dictionary.containsKey(wc))
                w = wc;
            else {
                Integer di = dictionary.get(w);
                if (di == null) {
                    dictionary.put(w, dictSize++);
                }
                result.add(di);
                // Add wc to the dictionary.
                dictionary.put(wc, dictSize++);
                w = "" + c; //$NON-NLS-1$
            }
        }

        // Output the code for w.
        if (!w.equals("")) //$NON-NLS-1$
            result.add(dictionary.get(w));

        int[] result2 = new int[result.size()];
        for (int i = 0; i < result2.length; i++) {
            int j = result.get(i);
            result2[i] = j;
        }
        return result2;
    }

    /** Decompress a list of output ks to a string. */
    public static String decompress(int[] compressed) {
        // Build the dictionary.
        int dictSize = 256;
        Map<Integer,String> dictionary = new HashMap<Integer,String>();
        for (int i = 0; i < 256; i++)
            dictionary.put(i, "" + (char)i); //$NON-NLS-1$

        String w = "" + (char) compressed[0]; //$NON-NLS-1$
        StringBuffer result = new StringBuffer(w);
        final int size = compressed.length;
        for (int i = 1; i < size; i++) {
            int k = compressed[i];
            String entry;
            if (dictionary.containsKey(k))
                entry = dictionary.get(k);
            else if (k == dictSize)
                entry = w + w.charAt(0);
            else {
                dictionary.put(dictSize++, w);
                result.append(w);
                continue;
            }


            result.append(entry);

            // Add w+entry[0] to the dictionary.
            dictionary.put(dictSize++, w + entry.charAt(0));

            w = entry;
        }
        return result.toString();
    }




}
