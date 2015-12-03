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

import java.util.ArrayList;

import org.nschmidt.ldparteditor.helpers.compositetext.SubfileCompiler;
import org.nschmidt.ldparteditor.logger.NLogger;

public class VM00Snapshot extends VM00Base {

    protected VM00Snapshot(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public ArrayList<MemorySnapshot> getSnapshots() {
        return snapshots;
    }

    public void addSnapshot() {
        if (NLogger.DEBUG) {
            MemorySnapshot snapshot = new MemorySnapshot(linkedDatFile);
            getSnapshots().add(snapshot);
            NLogger.debug(getClass(), "CREATED SNAPSHOT ON {0} ", snapshot.toString()); //$NON-NLS-1$
        }
    }

    public void loadSnapshot(MemorySnapshot s) {
        if (NLogger.DEBUG) {
            clear();
            GData0 emptyLine = new GData0(""); //$NON-NLS-1$
            linkedDatFile.getDrawPerLine_NOCLONE().clear();
            linkedDatFile.getDrawChainStart().setNext(emptyLine);
            linkedDatFile.getDrawPerLine_NOCLONE().put(1, emptyLine);
            setModified(true, false);
            StringBuilder sb = new StringBuilder();
            for (String line : s.getBackup()) {
                sb.append("\n"); //$NON-NLS-1$
                sb.append(line);
            }
            sb.deleteCharAt(0);
            linkedDatFile.setText(sb.toString());
            SubfileCompiler.compile(linkedDatFile, false, true);
        }
    }
}
