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
import java.util.HashMap;
import java.util.Set;

class VM11HideShow extends VM10Selector {

    private HashMap<String, ArrayList<Boolean>> state = new HashMap<String, ArrayList<Boolean>>();

    protected VM11HideShow(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public synchronized void showHidden() {
        for (GData gd : dataToHide) {
            gd.show();
        }
        dataToHide.clear();
    }

    public void hideSelection() {
        for (GData1 data : selectedSubfiles) {
            hide(data);
        }
        for (GData2 data : selectedLines) {
            hide(data);
        }
        for (GData3 data : selectedTriangles) {
            hide(data);
        }
        for (GData4 data : selectedQuads) {
            hide(data);
        }
        for (GData5 data : selectedCondlines) {
            hide(data);
        }
        for (Vertex vert : selectedVertices) {
            Set<VertexManifestation> m = vertexLinkedToPositionInFile.get(vert);
            boolean isHidden = true;
            for (VertexManifestation vm : m) {
                if (vm.getGdata().type() != 0 && vm.getGdata().visible) {
                    isHidden = false;
                    break;
                }
            }
            if (isHidden)
                hiddenVertices.add(vert);
        }
        clearSelection();
    }

    private void hide(GData gdata) {
        gdata.hide();
        hiddenData.add(gdata);
    }

    public void showAll() {
        cleanupHiddenData();
        for (GData ghost : hiddenData) {
            ghost.show();
        }
        hiddenVertices.clear();
        hiddenData.clear();
    }

    public HashMap<String, ArrayList<Boolean>> backupHideShowState() {
        state.clear();
        if (hiddenData.size() > 0) {
            backup(linkedDatFile.getDrawChainStart(), state, 0, 0);
            return state;
        }
        return null;
    }

    private void backup(GData g, HashMap<String, ArrayList<Boolean>> s, int depth, int currentLine) {
        final ArrayList<Boolean> st = new ArrayList<Boolean>();
        int lineNumber = 1;
        ++depth;
        final String key;
        if (depth == 1) {
            key = "TOP"; //$NON-NLS-1$
        } else {
            GData1 g1 = ((GDataInit) g).getParent();
            key = depth + "|" + currentLine + " " + g1.shortName; //$NON-NLS-1$ //$NON-NLS-2$
        }
        state.put(key, st);
        st.add(g.visible);
        while ((g = g.getNext()) != null) {
            st.add(g.visible);
            if (g.type() == 1) {
                backup(((GData1) g).myGData, s, depth, lineNumber);
            }
            lineNumber++;
        }
    }

    public void restoreHideShowState() {
        if (state.size() > 0) {
            restore(linkedDatFile.getDrawChainStart(), state, 0, 0);
            state.clear();
        }
    }

    private void restore(GData g, HashMap<String, ArrayList<Boolean>> s, int depth, int currentLine) {
        int lineNumber = 1;
        ++depth;
        final String key;
        if (depth == 1) {
            key = "TOP"; //$NON-NLS-1$
        } else {
            GData1 g1 = ((GDataInit) g).getParent();
            key = depth + "|" + currentLine + " " + g1.shortName; //$NON-NLS-1$ //$NON-NLS-2$
        }
        ArrayList<Boolean> nl = new ArrayList<Boolean>();
        nl.add(true);
        s.putIfAbsent(key, nl);
        final ArrayList<Boolean> st = s.get(key);
        final int size = st.size();
        g.visible = st.get(0);
        if (!g.visible) hiddenData.add(g);
        while ((g = g.getNext()) != null) {
            if (lineNumber < size) {
                g.visible = st.get(lineNumber);
            } else {
                g.visible = true;
            }
            if (!g.visible) hiddenData.add(g);
            if (g.type() == 1) {
                restore(((GData1) g).myGData, s, depth, lineNumber);
            }
            lineNumber++;
        }
    }
}
