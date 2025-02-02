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
import java.util.List;
import java.util.Map;
import java.util.Set;

class VM11HideShow extends VM10Selector {

    private Map<String, List<Boolean>> state = new HashMap<>();

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
            if (m == null) continue;
            for (VertexManifestation vm : m) {
                if (vm.gdata().type() != 0 && vm.gdata().visible) {
                    isHidden = false;
                    break;
                }
            }
            if (isHidden)
                hiddenVertices.add(vert);
        }
        clearSelection();
    }

    public void showSelection() {
        for (GData1 data : selectedSubfiles) {
            show(data);
        }
        for (GData2 data : selectedLines) {
            show(data);
        }
        for (GData3 data : selectedTriangles) {
            show(data);
        }
        for (GData4 data : selectedQuads) {
            show(data);
        }
        for (GData5 data : selectedCondlines) {
            show(data);
        }
        hiddenVertices.removeAll(selectedVertices);
        clearSelection();
    }

    private void hide(GData gdata) {
        gdata.hide();
        hiddenData.add(gdata);
    }

    private void show(GData gdata) {
        gdata.show();
        hiddenData.remove(gdata);
    }

    public void showAll() {
        for (GData1 ghost : vertexCountInSubfile.keySet()) {
            ghost.show();
        }
        for (GData2 ghost : lines.keySet()) {
            ghost.show();
        }
        for (GData3 ghost : triangles.keySet()) {
            ghost.show();
        }
        for (GData4 ghost : quads.keySet()) {
            ghost.show();
        }
        for (GData5 ghost : condlines.keySet()) {
            ghost.show();
        }
        hiddenVertices.clear();
        hiddenData.clear();
    }

    public Map<String, List<Boolean>> backupHideShowState() {
        state.clear();
        if (!hiddenData.isEmpty()) {
            backup(linkedDatFile.getDrawChainStart(), state, ""); //$NON-NLS-1$
            return state;
        }
        return new HashMap<>();
    }

    public Map<String, List<Boolean>> backupHideShowState(Map<String, List<Boolean>> s) {
        if (!hiddenData.isEmpty()) {
            backup(linkedDatFile.getDrawChainStart(), s, ""); //$NON-NLS-1$
            return s;
        }
        return new HashMap<>();
    }

    private void backup(GData g, Map<String, List<Boolean>> s, String key) {
        final List<Boolean> st = new ArrayList<>();
        int lineNumber = 1;
        s.put(key, st);
        st.add(g.visible);
        while ((g = g.getNext()) != null) {
            final int type = g.type();
            if (type > 0 && type < 6) {
                st.add(g.visible);
                if (type == 1) {
                    backup(((GData1) g).myGData, s, key + "|" + lineNumber); //$NON-NLS-1$
                }
                lineNumber++;
            }
        }
    }

    public Map<String, List<Boolean>> backupSelectedDataState(Map<String, List<Boolean>> s) {
        if (!selectedData.isEmpty()) {
            backup2(linkedDatFile.getDrawChainStart(), s, ""); //$NON-NLS-1$
            return s;
        }
        return new HashMap<>();
    }

    private void backup2(GData g, Map<String, List<Boolean>> s, String key) {
        final List<Boolean> st = new ArrayList<>();
        int lineNumber = 1;
        s.put(key, st);
        st.add(selectedData.contains(g));
        while ((g = g.getNext()) != null) {
            final int type = g.type();
            if (type > 0 && type < 6) {
                st.add(selectedData.contains(g));
                if (type == 1) {
                    backup2(((GData1) g).myGData, s, key + "|" + lineNumber); //$NON-NLS-1$
                }
                lineNumber++;
            }
        }
    }

    public void backupHideShowAndSelectedState(Map<String, List<Boolean>> s1, Map<String, List<Boolean>> s2) {
        backup3(linkedDatFile.getDrawChainStart(), s1, s2, ""); //$NON-NLS-1$
    }

    private void backup3(GData g, Map<String, List<Boolean>> s1, Map<String, List<Boolean>> s2, String key) {
        final List<Boolean> st1 = new ArrayList<>();
        final List<Boolean> st2 = new ArrayList<>();
        int lineNumber = 1;
        s1.put(key, st1);
        s2.put(key, st2);
        st1.add(g.visible);
        st2.add(selectedData.contains(g));
        while ((g = g.getNext()) != null) {
            final int type = g.type();
            if (type > 0 && type < 6) {
                st1.add(g.visible);
                st2.add(selectedData.contains(g));
                if (type == 1) {
                    backup3(((GData1) g).myGData, s1, s2, key + "|" + lineNumber); //$NON-NLS-1$
                }
                lineNumber++;
            }
        }
    }

    public void restoreHideShowState() {
        if (state.size() > 0) {
            restore(linkedDatFile.getDrawChainStart(), state, ""); //$NON-NLS-1$
            state.clear();
        }
    }

    public void restoreHideShowState(Map<String, List<Boolean>> s) {
        state.clear();
        state.putAll(s);
        restoreHideShowState();
    }

    private void restore(GData g, Map<String, List<Boolean>> s, String key) {
        int lineNumber = 1;
        List<Boolean> nl = new ArrayList<>();
        nl.add(true);
        s.putIfAbsent(key, nl);
        final List<Boolean> st = s.get(key);
        final int size = st.size();
        g.visible = st.get(0);
        if (!g.visible) hiddenData.add(g);
        while ((g = g.getNext()) != null) {
            final int type = g.type();
            if (type > 0 && type < 6) {
                if (lineNumber < size) {
                    g.visible = st.get(lineNumber);
                } else {
                    g.visible = true;
                }
                if (!g.visible) hiddenData.add(g);
                if (type  == 1) {
                    restore(((GData1) g).myGData, s, key + "|" + lineNumber); //$NON-NLS-1$
                }
                lineNumber++;
            }
        }
    }

    public void restoreSelectedDataState(Map<String, List<Boolean>> s) {
        if (s.size() > 0) {
            restore2(linkedDatFile.getDrawChainStart(), s, ""); //$NON-NLS-1$
        }
    }

    private void restore2(GData g, Map<String, List<Boolean>> s, String key) {
        int lineNumber = 1;
        List<Boolean> nl = new ArrayList<>();
        nl.add(true);
        s.putIfAbsent(key, nl);
        final List<Boolean> st = s.get(key);
        final int size = st.size();
        if (Boolean.TRUE.equals(st.get(0))) {
            selectedData.add(g);
            switch (g.type()) {
            case 1:
                selectedSubfiles.add((GData1) g);
                break;
            case 2:
                selectedLines.add((GData2) g);
                break;
            case 3:
                selectedTriangles.add((GData3) g);
                break;
            case 4:
                selectedQuads.add((GData4) g);
                break;
            case 5:
                selectedCondlines.add((GData5) g);
                break;
            default:
                break;
            }
        }
        while ((g = g.getNext()) != null) {
            final int type = g.type();
            if (type > 0 && type < 6) {
                if (lineNumber < size && Boolean.TRUE.equals(st.get(lineNumber))) {
                    selectedData.add(g);
                    switch (type) {
                    case 1:
                        selectedSubfiles.add((GData1) g);
                        break;
                    case 2:
                        selectedLines.add((GData2) g);
                        break;
                    case 3:
                        selectedTriangles.add((GData3) g);
                        break;
                    case 4:
                        selectedQuads.add((GData4) g);
                        break;
                    case 5:
                        selectedCondlines.add((GData5) g);
                        break;
                    default:
                        break;
                    }
                }
                if (type  == 1) {
                    restore2(((GData1) g).myGData, s, key + "|" + lineNumber); //$NON-NLS-1$
                }
                lineNumber++;
            }
        }
    }
}
