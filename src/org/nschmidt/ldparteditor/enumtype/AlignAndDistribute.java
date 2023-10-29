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
package org.nschmidt.ldparteditor.enumtype;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexInfo;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.helper.SelectionGroup;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToggleToolItem;

public enum AlignAndDistribute {
    X, X_AVG, X_MAX, X_MIN, Y, Y_AVG, Y_MAX, Y_MIN, Z, Z_AVG, Z_MAX, Z_MIN;
    
    public static void align(AlignAndDistribute axis) {
        final Composite3D c3d = Editor3DWindow.getWindow().getCurrentCoposite3d();
        if (c3d != null) {
            final DatFile df = c3d.getLockableDatFileReference();
            if (df.isReadOnly()) return;
            final VertexManager vm = df.getVertexManager();
            NLogger.debug(AlignAndDistribute.class, "Align on axis {0}.", axis); //$NON-NLS-1$
            final List<SelectionGroup> groups = addSnapshotAndPrepareGroupSelection(vm);
            NLogger.debug(AlignAndDistribute.class, "Identified {0} selected group(s) to align.", groups.size()); //$NON-NLS-1$
            calcuateMinMaxAvgForGroups(groups, vm, axis);
            // FIXME Needs implementation!
            
            finishModification(vm, groups);
        }
    }

    public static void distribute(AlignAndDistribute axis) {
        final Composite3D c3d = Editor3DWindow.getWindow().getCurrentCoposite3d();
        if (c3d != null) {
            final DatFile df = c3d.getLockableDatFileReference();
            if (df.isReadOnly()) return;
            final VertexManager vm = df.getVertexManager();
            NLogger.debug(AlignAndDistribute.class, "Distribute on axis {0}.", axis); //$NON-NLS-1$
            final List<SelectionGroup> groups = addSnapshotAndPrepareGroupSelection(vm);
            NLogger.debug(AlignAndDistribute.class, "Identified {0} selected group(s) to distribute.", groups.size()); //$NON-NLS-1$
            calcuateMinMaxAvgForGroups(groups, vm, axis);
            // FIXME Needs implementation!
            
            finishModification(vm, groups);
        }
    }
    
    public static void distributeEqually(AlignAndDistribute axis) {
        final Composite3D c3d = Editor3DWindow.getWindow().getCurrentCoposite3d();
        if (c3d != null) {
            final DatFile df = c3d.getLockableDatFileReference();
            if (df.isReadOnly()) return;
            final VertexManager vm = df.getVertexManager();
            NLogger.debug(AlignAndDistribute.class, "Distribute equally on axis {0}.", axis); //$NON-NLS-1$
            final List<SelectionGroup> groups = addSnapshotAndPrepareGroupSelection(vm);
            NLogger.debug(AlignAndDistribute.class, "Identified {0} selected group(s) for equal distribution.", groups.size()); //$NON-NLS-1$
            calcuateMinMaxAvgForGroups(groups, vm, axis);
            // FIXME Needs implementation!
            
            finishModification(vm, groups);
        }
    }
    
    private static void calcuateMinMaxAvgForGroups(final List<SelectionGroup> groups, final VertexManager vm, final AlignAndDistribute axis) {
        final List<SelectionGroup> groupsToDelete = new ArrayList<>();
        final boolean xAxis = axis == X || axis == X_MIN || axis == X_MAX || axis == X_AVG;
        final boolean yAxis = axis == Y || axis == Y_MIN || axis == Y_MAX || axis == Y_AVG;
        final boolean zAxis = axis == Z || axis == Z_MIN || axis == Z_MAX || axis == Z_AVG;
        for (SelectionGroup selectionGroup : groups) {
            final Set<Vertex> vertices = new TreeSet<>();
            final Map<GData, Set<VertexInfo>> llv = vm.getLineLinkedToVertices();
            
            BigDecimal min = null;
            BigDecimal max = null;
            BigDecimal avg = null;
            
            for (GData data : selectionGroup.group()) {
                final Set<VertexInfo> vis = llv.getOrDefault(data, Set.of());
                for (VertexInfo vi : vis) {
                    // Don't look at condline control points
                    if (data.type() == 5 && vi.getPosition() > 1) continue;
                    final Vertex v = vi.getVertex();
                    if (xAxis && (min != null && min.compareTo(v.xp) > 0 || min == null)) {
                        min = v.xp;
                    }
                    
                    if (yAxis && (min != null && min.compareTo(v.yp) > 0 || min == null)) {
                        min = v.yp;
                    }
                    
                    if (zAxis && (min != null && min.compareTo(v.zp) > 0 || min == null)) {
                        min = v.zp;
                    }
                    
                    if (xAxis && (max != null && max.compareTo(v.xp) < 0 || max == null)) {
                        max = v.xp;
                    }
                    
                    if (yAxis && (max != null && max.compareTo(v.yp) < 0 || max == null)) {
                        max = v.yp;
                    }
                    
                    if (zAxis && (max != null && max.compareTo(v.zp) < 0 || max == null)) {
                        max = v.zp;
                    }
                    
                    vertices.add(v);
                }
            }
            
            final int size = vertices.size();
            if (size > 0) {
                avg = BigDecimal.ZERO;
                for (Vertex vertex : vertices) {
                    if (xAxis) avg = avg.add(vertex.xp);
                    if (yAxis) avg = avg.add(vertex.yp);
                    if (zAxis) avg = avg.add(vertex.zp);
                }
                
                avg = avg.divide(BigDecimal.valueOf(size), Threshold.MC);
                NLogger.debug(AlignAndDistribute.class, "Min : {0} of group {1}", min, selectionGroup); //$NON-NLS-1$
                NLogger.debug(AlignAndDistribute.class, "Max : {0}", max); //$NON-NLS-1$
                NLogger.debug(AlignAndDistribute.class, "Avg : {0}", avg); //$NON-NLS-1$
            } else {
                // Remove this group because it does not contain a vertex (should not happen)
                groupsToDelete.add(selectionGroup);
                NLogger.debug(AlignAndDistribute.class, "Group was deleted : {0}", selectionGroup); //$NON-NLS-1$
            }
            
            
            selectionGroup.min()[0] = min;
            selectionGroup.max()[0] = max;
            selectionGroup.avg()[0] = avg;
        }
        
        groups.removeAll(groupsToDelete);
    }

    private static List<SelectionGroup> addSnapshotAndPrepareGroupSelection(final VertexManager vm) {
        vm.addSnapshot();
        vm.skipSyncTimer();
        return calculateSelectionGroups(vm);
    }
    
    private static void finishModification(final VertexManager vm, final List<SelectionGroup> groups) {
        final Set<GData1> selectedSubfiles = new TreeSet<>(vm.getSelectedSubfiles());
        if (!selectedSubfiles.isEmpty()) {
            vm.reSelectSubFiles();
        }
        
        if (!groups.isEmpty()) {
            NLogger.debug(AlignAndDistribute.class, "Action done."); //$NON-NLS-1$
            vm.validateState();
            vm.setModified(true, true);
        }
    }
    
    private static void removeThisMethodLater(final VertexManager vm) {
        // Make sure to snap the vertices of selected objects
        
        final float gridSize = 0;
        final Set<Vertex> selectedVertices = new TreeSet<>(vm.getSelectedVertices());
        final Set<GData1> selectedSubfiles = new TreeSet<>(vm.getSelectedSubfiles());
        
        final BigDecimal gridSizePrecise = BigDecimal.valueOf(gridSize);
        
        for (Vertex v : selectedVertices) {
            // Don't snap the vertex if it is a subfile vertex.
            final Set<GData> linkedData = vm.getLinkedSurfacesSubfilesAndLines(v);
            if (linkedData.stream().anyMatch(g -> g.type() == 1)) continue;
            
            final BigDecimal newX = BigDecimal.ZERO;
            final BigDecimal newY = BigDecimal.ZERO;
            final BigDecimal newZ = BigDecimal.ZERO;
            
            vm.changeVertexDirectFast(v, new Vertex(new BigDecimal[]{newX, newY, newZ}), true);
        }
        
        for (GData1 g : selectedSubfiles) {
            final Matrix subM = g.getAccurateProductMatrix();
            final BigDecimal newX = BigDecimal.ZERO;
            final BigDecimal newY = BigDecimal.ZERO;
            final BigDecimal newZ = BigDecimal.ZERO;
            
            final Matrix m = View.ACCURATE_ID.translate(new BigDecimal[]{newX, newY, newZ});
            
            vm.backupSelection();
            vm.clearSelection2();
            vm.getSelectedSubfiles().add(g);
            vm.reSelectSubFiles();
            vm.transformSelection(m, null, MiscToggleToolItem.isMovingAdjacentData(), false);
            final GData1 sub = vm.getSelectedSubfiles().stream().findAny().orElse(null);
            vm.restoreSelection();
            if (sub != null) {
                vm.getSelectedSubfiles().add(sub);
            }
        }
    }

    private static List<SelectionGroup> calculateSelectionGroups(final VertexManager vm) {
        final List<SelectionGroup> result = new ArrayList<>();
        // If "Move Adjacent Data" is on, then adjacent selected data (n) should be considered as a group. This will cause O(n²) complexity.
        // Subfile content should be ignored, since it can't be transformed (only the subfile as a whole can be moved)
        vm.validateState();
        final SequencedSet<GData> selection = new TreeSet<>(vm.getSelectedData());
        // Add selected vertex meta commands
        for (Vertex vert : vm.getSelectedVertices()) {
            vm.getLinkedVertexMetaCommands(vert).stream()
                .findFirst().ifPresent(selection::add);
        }
        NLogger.debug(AlignAndDistribute.class, "=> Original selection contains {0} object(s).", selection.size()); //$NON-NLS-1$
        selection.removeIf(data -> !vm.getLineLinkedToVertices().containsKey(data));
        NLogger.debug(AlignAndDistribute.class, "=> Selection without subfile data contains {0} object(s).", selection.size()); //$NON-NLS-1$
        
        final boolean moveAdjacentData = MiscToggleToolItem.isMovingAdjacentData();
        if (moveAdjacentData) {
            final Map<GData, List<GData>> groups = new HashMap<>();
            int i = 0;
            for (GData dataA : selection) {
                final Set<VertexInfo> vertsA = vm.getLineLinkedToVertices().getOrDefault(dataA, Set.of());
                int j = 0;
                for (GData dataB : selection) {
                    if (j <= i) {
                        j++;
                        continue;
                    }
                    
                    final Set<VertexInfo> vertsB = vm.getLineLinkedToVertices().getOrDefault(dataB, Set.of());
                    
                    for (VertexInfo viA : vertsA) {
                        // Don't look at condline control points
                        if (dataA.type() == 5 && viA.getPosition() > 1) continue;
                        for (VertexInfo viB : vertsB) {
                            if (dataB.type() == 5 && viB.getPosition() > 1) continue;
                            final Vertex vA = viA.getVertex();
                            final Vertex vB = viB.getVertex();
                            // isNeighbour? Does it really make sense to check for neighbors?
                            // They are only connected via a connecting edge, they do not share a common vertex. 
                            if (vA.compareTo(vB) == 0) { // or vm.isNeighbour(vA, vB)
                                if (!groups.containsKey(dataA) && !groups.containsKey(dataB)) {
                                    final List<GData> group = new ArrayList<>(2);
                                    group.add(dataA);
                                    group.add(dataB);
                                    groups.put(dataA, group);
                                    groups.put(dataB, group);
                                } else if (groups.containsKey(dataA) && groups.containsKey(dataB)) {
                                    // Don't add them if there already part of a group
                                } else if (groups.containsKey(dataA)) {
                                    final List<GData> group = groups.get(dataA);
                                    group.add(dataB);
                                    groups.put(dataB, group);
                                } else if (groups.containsKey(dataB)) {
                                    final List<GData> group = groups.get(dataB);
                                    group.add(dataA);
                                    groups.put(dataA, group);
                                }
                            }
                        }
                    }
                    
                    j++;
                }
                
                i++;
            }
            
            // We determined all adjacent groups
            result.addAll(new HashSet<>(groups.values()).stream().map(AlignAndDistribute::asSelectionGroup).toList());
            
            // Data without adjacency stands for itself
            for (GData data : selection) {
                if (groups.containsKey(data)) continue;
                result.add(asSelectionGroup(List.of(data)));
            }
        } else {
            // Every object stands for itself (no adjacency)
            for (GData data : selection) {
                result.add(asSelectionGroup(List.of(data)));
            }
        }
        
        return result;
    }
    
    private static SelectionGroup asSelectionGroup(final List<GData> group) {
        return new SelectionGroup(group, new BigDecimal[1], new BigDecimal[1], new BigDecimal[1]);
    }
}
