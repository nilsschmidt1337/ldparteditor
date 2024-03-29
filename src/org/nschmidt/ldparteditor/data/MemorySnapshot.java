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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.nschmidt.ldparteditor.logger.NLogger;

public class MemorySnapshot {

    private final String creation;
    private final String[] backup;

    MemorySnapshot(DatFile df) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy h:mm:ss a"); //$NON-NLS-1$
        String formattedDate = sdf.format(date);
        final int objCount = df.getDrawPerLineNoClone().size();
        creation =  formattedDate + " (" + objCount + " Objects)"; //$NON-NLS-1$ //$NON-NLS-2$
        String[] backupArray = new String[objCount];
        int count = 0;
        GData data2draw = df.getDrawChainStart();
        while (count < objCount && data2draw != null) {
            data2draw = data2draw.getNext();
            if (data2draw == null) {
                NLogger.debug(MemorySnapshot.class, "Unexpected end of file. It has {0} linked objects, but {1} lines", count, objCount); //$NON-NLS-1$
                break;
            }

            backupArray[count] = data2draw.toString();
            count++;
        }

        if (count < objCount) {
            String[] smallerBackup = new String[count];
            System.arraycopy(backupArray, 0, smallerBackup, 0, count);

            backupArray = smallerBackup;
        }

        this.backup = backupArray;
    }

    @Override
    public String toString() {
        return creation;
    }

    public String[] getBackup() {
        return backup;
    }

}
