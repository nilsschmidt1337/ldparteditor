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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.nschmidt.ldparteditor.helpers.math.PowerRay;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;

/**
 * @author nils
 *
 */
class VMBase {

    protected final ArrayList<MemorySnapshot> snapshots = new ArrayList<MemorySnapshot>();

    // 1 Vertex kann an mehreren Stellen (GData2-5 + position) manifestiert sein
    /**
     * Subfile-Inhalte sind hierbei enthalten. Die Manifestierung gegen
     * {@code lineLinkedToVertices} checken, wenn ausgeschlossen werden soll,
     * dass es sich um Subfile Daten handelt
     */
    protected final ThreadsafeTreeMap<Vertex, Set<VertexManifestation>> vertexLinkedToPositionInFile = new ThreadsafeTreeMap<Vertex, Set<VertexManifestation>>();

    // 1 Vertex kann keinem oder mehreren Subfiles angehören
    protected final ThreadsafeTreeMap<Vertex, Set<GData1>> vertexLinkedToSubfile = new ThreadsafeTreeMap<Vertex, Set<GData1>>();

    // Auf Dateiebene: 1 Vertex kann an mehreren Stellen (GData1-5 + position)
    // manifestiert sein, ist er auch im Subfile, so gibt VertexInfo dies an
    /** Subfile-Inhalte sind hier nicht als Key refenziert!! */
    protected final ThreadsafeHashMap<GData, Set<VertexInfo>> lineLinkedToVertices = new ThreadsafeHashMap<GData, Set<VertexInfo>>();

    public ThreadsafeHashMap<GData, Set<VertexInfo>> getLineLinkedToVertices() {
        return lineLinkedToVertices;
    }

    protected final TreeMap<Vertex, float[]> vertexLinkedToNormalCACHE = new TreeMap<Vertex, float[]>();
    protected final HashMap<GData, float[]> dataLinkedToNormalCACHE = new HashMap<GData, float[]>();

    protected final ThreadsafeHashMap<GData1, Integer> vertexCountInSubfile = new ThreadsafeHashMap<GData1, Integer>();

    protected final ThreadsafeHashMap<GData0, Vertex[]> declaredVertices = new ThreadsafeHashMap<GData0, Vertex[]>();
    protected final ThreadsafeHashMap<GData2, Vertex[]> lines = new ThreadsafeHashMap<GData2, Vertex[]>();
    protected final ThreadsafeHashMap<GData3, Vertex[]> triangles = new ThreadsafeHashMap<GData3, Vertex[]>();
    protected final ThreadsafeHashMap<GData4, Vertex[]> quads = new ThreadsafeHashMap<GData4, Vertex[]>();
    protected final ThreadsafeHashMap<GData5, Vertex[]> condlines = new ThreadsafeHashMap<GData5, Vertex[]>();

    protected final Vertex[] vArray = new Vertex[4];
    protected final VertexManifestation[] vdArray = new VertexManifestation[4];

    protected final Set<Vertex> selectedVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

    protected final Set<GData> selectedData = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());
    protected final Set<GData1> selectedSubfiles = Collections.newSetFromMap(new ThreadsafeHashMap<GData1, Boolean>());
    protected final Set<GData2> selectedLines = Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
    protected final Set<GData3> selectedTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
    protected final Set<GData4> selectedQuads = Collections.newSetFromMap(new ThreadsafeHashMap<GData4, Boolean>());
    protected final Set<GData5> selectedCondlines = Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());

    protected final Set<Vertex> backupSelectedVertices = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());

    protected final Set<GData> backupSelectedData = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());
    protected final Set<GData1> backupSelectedSubfiles = Collections.newSetFromMap(new ThreadsafeHashMap<GData1, Boolean>());
    protected final Set<GData2> backupSelectedLines = Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
    protected final Set<GData3> backupSelectedTriangles = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
    protected final Set<GData4> backupSelectedQuads = Collections.newSetFromMap(new ThreadsafeHashMap<GData4, Boolean>());
    protected final Set<GData5> backupSelectedCondlines = Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());

    protected final Set<GData> newSelectedData = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());

    protected GDataPNG selectedBgPicture = null;
    protected int selectedBgPictureIndex = -1;

    protected final Set<Vertex> selectedVerticesForSubfile = Collections.newSetFromMap(new ThreadsafeTreeMap<Vertex, Boolean>());
    protected final Set<GData2> selectedLinesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData2, Boolean>());
    protected final Set<GData3> selectedTrianglesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData3, Boolean>());
    protected final Set<GData4> selectedQuadsForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData4, Boolean>());
    protected final Set<GData5> selectedCondlinesForSubfile = Collections.newSetFromMap(new ThreadsafeHashMap<GData5, Boolean>());

    protected static final List<GData> CLIPBOARD = new ArrayList<GData>();
    protected static final Set<GData> CLIPBOARD_InvNext = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());

    protected final Set<GData> dataToHide = Collections.newSetFromMap(new ThreadsafeHashMap<GData, Boolean>());

    protected final PowerRay powerRay = new PowerRay();

    protected final DatFile linkedDatFile;

    protected Vertex vertexToReplace = null;

    protected boolean modified = false;
    protected boolean updated = true;

    protected AtomicBoolean skipSyncWithTextEditor = new AtomicBoolean(false);

    protected int selectedItemIndex = -1;
    protected GData selectedLine = null;

    protected Vertex lastSelectedVertex = null;

    public VMBase(DatFile linkedDatFile) {
        this.linkedDatFile = linkedDatFile;
    }
}
