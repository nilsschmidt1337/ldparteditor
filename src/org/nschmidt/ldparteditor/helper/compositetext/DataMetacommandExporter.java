package org.nschmidt.ldparteditor.helper.compositetext;

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

import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GDataBinary;
import org.nschmidt.ldparteditor.helper.math.HashBiMap;

/**
 * Exports !DATA meta-commands to a file (PNG only)
 */
public enum DataMetacommandExporter {
    INSTANCE;

    /**
     * Exports selected !DATA meta-commands to a file (PNG only)
     *
     * @param lineStart
     *            start line number to export
     * @param lineEnd
     *            end line number to export
     * @param datFile
     */
    public static void export(int lineStart, int lineEnd, DatFile datFile) {
        HashBiMap<Integer, GData> dpl = datFile.getDrawPerLineNoClone();
        lineEnd++;
        for (int line = lineStart; line < lineEnd; line++) {
            final GData data = dpl.getValue(line);
            if (data instanceof GDataBinary gd) {
                // TODO Needs implementation!
                System.out.println(data);
            }
        }
    }
}
