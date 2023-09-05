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
package org.nschmidt.ldparteditor.data;

import java.util.HashMap;
import java.util.Map;

import org.nschmidt.ldparteditor.logger.NLogger;

class BinaryDataRegistry {
    
    private final Map<String, GDataBinary> fileNameToMetaTagMap = new HashMap<>();
    private final Map<String, byte[]> fileNameToBinaryMap = new HashMap<>();

    public boolean hasFile(String filename) {
        return fileNameToMetaTagMap.containsKey(filename)
            || fileNameToBinaryMap.containsKey(filename);
    }

    public byte[] getFileBytes(String filename) {
        if (fileNameToBinaryMap.containsKey(filename)) {
            return fileNameToBinaryMap.getOrDefault(filename, new byte[0]);
        }
        
        final GDataBinary dataMetaTag = fileNameToMetaTagMap.get(filename);
        final byte[] binary = dataMetaTag.loadBinary();
        fileNameToBinaryMap.put(filename, binary);
        fileNameToMetaTagMap.remove(filename);
        
        return binary;
    }

    public void addData(GDataBinary dataMetaTag) {
        NLogger.debug(BinaryDataRegistry.class, "Register binary !DATA: {0}", dataMetaTag); //$NON-NLS-1$
        // "0 !DATA " has a length of 8
        final String filename = dataMetaTag.toString().substring(8);
        NLogger.debug(BinaryDataRegistry.class, "The filename is: {0}", filename); //$NON-NLS-1$
        fileNameToMetaTagMap.put(filename, dataMetaTag);
        fileNameToBinaryMap.remove(filename);
    }
}
