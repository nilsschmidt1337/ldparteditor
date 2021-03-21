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
package org.nschmidt.ldparteditor.workbench;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.nschmidt.ldparteditor.data.PGData;
import org.nschmidt.ldparteditor.data.PGTimestamp;

/**
 * This class represents the primitive cache
 *
 * @author nils
 *
 */
public class PrimitiveCache implements Serializable {
    // Do not rename fields. It will break backwards compatibility! New values, which were not included in the state before, have to be initialized! (@ WorkbenchManager.loadWorkbench())

    /** V1.00 */
    private static final long serialVersionUID = 1L;

    private HashMap<String, PGData> primitiveCache = new HashMap<>();
    private HashMap<PGTimestamp, ArrayList<String>> primitiveFileCache = new HashMap<>();

    public HashMap<String, PGData> getPrimitiveCache() {
        return primitiveCache;
    }

    public void setPrimitiveCache(HashMap<String, PGData> primitiveCache) {
        this.primitiveCache = primitiveCache;
    }

    public HashMap<PGTimestamp, ArrayList<String>> getPrimitiveFileCache() {
        return primitiveFileCache;
    }

    public void setPrimitiveFileCache(HashMap<PGTimestamp, ArrayList<String>> primitiveFileCache) {
        this.primitiveFileCache = primitiveFileCache;
    }
}
