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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;
import java.util.TreeMap;
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
            if (groups.isEmpty()) return;
            calculateMinMaxAvgForGroups(groups, vm, axis);
            final boolean alignOnX = axis == X_AVG || axis == X_MAX || axis == X_MIN;
            final boolean alignOnY = axis == Y_AVG || axis == Y_MAX || axis == Y_MIN;
            final boolean alignOnZ = axis == Z_AVG || axis == Z_MAX || axis == Z_MIN;
            final boolean alignAvg = axis == X_AVG || axis == Y_AVG || axis == Z_AVG;
            final boolean alignMin = axis == X_MIN || axis == Y_MIN || axis == Z_MIN;
            final boolean alignMax = axis == X_MAX || axis == Y_MAX || axis == Z_MAX;
            
            BigDecimal avg = BigDecimal.ZERO;
            BigDecimal min = groups.get(0).min()[0];
            BigDecimal max = groups.get(0).max()[0];
            
            for (SelectionGroup group : groups) {
                if (group.min()[0].compareTo(min) < 0) {
                    min = group.min()[0];
                }
                if (group.max()[0].compareTo(max) > 0) {
                    max = group.max()[0];
                }
                
                avg = avg.add(group.avg()[0]);
            }
            
            avg = avg.divide(BigDecimal.valueOf(groups.size()), Threshold.MC);
            
            for (SelectionGroup group : groups) {
                final BigDecimal delta;
                if (alignAvg) {
                    // destination = average 
                    // source + delta = destination
                    // delta = destination - source
                    delta = avg.subtract(group.avg()[0]);
                } else if (alignMin) {
                    delta = min.subtract(group.min()[0]);
                } else if (alignMax) {
                    delta = max.subtract(group.max()[0]);
                } else {
                    delta = BigDecimal.ZERO;
                }
                
                vm.backupSelection();
                vm.clearSelection2();
                final Set<Integer> selectedLineNumbers = vm.addToSelection(group.group());
                final BigDecimal deltaX = alignOnX ? delta : BigDecimal.ZERO;
                final BigDecimal deltaY = alignOnY ? delta : BigDecimal.ZERO;
                final BigDecimal deltaZ = alignOnZ ? delta : BigDecimal.ZERO;
                
                final Matrix m = View.ACCURATE_ID.translate(new BigDecimal[]{deltaX, deltaY, deltaZ});
                vm.transformSelection(m, null, MiscToggleToolItem.isMovingAdjacentData(), false);
                vm.restoreSelection();
                for (int number : selectedLineNumbers) {
                    vm.addTextLineToSelection(number); 
                }
            }
            
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
            if (groups.size() <= 2) return;
            calculateMinMaxAvgForGroups(groups, vm, axis);
            final boolean distributeOnX = axis == X_AVG || axis == X_MAX || axis == X_MIN;
            final boolean distributeOnY = axis == Y_AVG || axis == Y_MAX || axis == Y_MIN;
            final boolean distributeOnZ = axis == Z_AVG || axis == Z_MAX || axis == Z_MIN;
            final boolean distributeAvg = axis == X_AVG || axis == Y_AVG || axis == Z_AVG;
            final boolean distributeMin = axis == X_MIN || axis == Y_MIN || axis == Z_MIN;
            final boolean distributeMax = axis == X_MAX || axis == Y_MAX || axis == Z_MAX;
            
            BigDecimal min;
            BigDecimal max;
            if (distributeMin) {
                min = groups.get(0).min()[0];
            } else if (distributeMax) {
                min = groups.get(0).max()[0];
            } else if (distributeAvg) {
                min = groups.get(0).avg()[0];
            } else {
                min = BigDecimal.ZERO;
            }
            
            max = min;
            
            for (SelectionGroup group : groups) {
                final BigDecimal value;
                if (distributeMin) {
                    value = group.min()[0];
                } else if (distributeMax) {
                    value = group.max()[0];
                } else if (distributeAvg) {
                    value = group.avg()[0];
                } else {
                    value = BigDecimal.ZERO;
                }
                
                if (value.compareTo(min) < 0) {
                    min = value;
                }
                if (value.compareTo(max) > 0) {
                    max = value;
                }
            }
            
            final BigDecimal span = max.subtract(min);
            final BigDecimal increment = span.divide(BigDecimal.valueOf(groups.size() - 1L), Threshold.MC);
            BigDecimal destination = min;
            
            groups.sort((a, b) -> {
                if (distributeAvg) {
                    return a.avg()[0].compareTo(b.avg()[0]);
                } else if (distributeMin) {
                    return a.min()[0].compareTo(b.min()[0]);
                } else if (distributeMax) {
                    return a.max()[0].compareTo(b.max()[0]);
                } else {
                    return a.avg()[0].compareTo(b.avg()[0]);
                }
            });
            
            for (SelectionGroup group : groups) {
                final BigDecimal delta;
                if (distributeAvg) {
                    // source + delta = destination
                    // delta = destination - source
                    delta = destination.subtract(group.avg()[0]);
                } else if (distributeMin) {
                    delta = destination.subtract(group.min()[0]);
                } else if (distributeMax) {
                    delta = destination.subtract(group.max()[0]);
                } else {
                    delta = BigDecimal.ZERO;
                }
                
                destination = destination.add(increment);
                
                vm.backupSelection();
                vm.clearSelection2();
                final Set<Integer> selectedLineNumbers = vm.addToSelection(group.group());
                final BigDecimal deltaX = distributeOnX ? delta : BigDecimal.ZERO;
                final BigDecimal deltaY = distributeOnY ? delta : BigDecimal.ZERO;
                final BigDecimal deltaZ = distributeOnZ ? delta : BigDecimal.ZERO;
                
                final Matrix m = View.ACCURATE_ID.translate(new BigDecimal[]{deltaX, deltaY, deltaZ});
                vm.transformSelection(m, null, MiscToggleToolItem.isMovingAdjacentData(), false);
                vm.restoreSelection();
                for (int number : selectedLineNumbers) {
                    vm.addTextLineToSelection(number); 
                }
            }
            
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
            if (groups.size() <= 2) return;
            calculateMinMaxAvgForGroups(groups, vm, axis);
            final boolean distributeOnX = axis == X;
            final boolean distributeOnY = axis == Y;
            final boolean distributeOnZ = axis == Z;
            
            BigDecimal min = groups.get(0).min()[0];
            BigDecimal max = min;
            
            BigDecimal selectionLength = BigDecimal.ZERO;
            
            for (SelectionGroup group : groups) {
                final BigDecimal minValue = group.min()[0];
                final BigDecimal maxValue = group.max()[0];
                if (minValue.compareTo(min) < 0) {
                    min = minValue;
                }
                if (maxValue.compareTo(max) > 0) {
                    max = maxValue;
                }
                
                selectionLength = selectionLength.add(maxValue.subtract(minValue));
            }
            
            groups.sort((a, b) -> a.min()[0].compareTo(b.min()[0]));
            
            final BigDecimal span = max.subtract(min);
            final BigDecimal freeSpace = span.subtract(selectionLength);
            final BigDecimal gapOrOverlapSize = freeSpace.divide(BigDecimal.valueOf(groups.size() - 1L), Threshold.MC);
            
            // When free space is greater zero, than we can distribute the gaps equally, otherwise we need to distribute the overlaps(!) equally.
            
            final int limit = groups.size() - 1;
            for (int i = 1; i < limit; i++)  {
                final SelectionGroup group = groups.get(i);
                final SelectionGroup previousGroup = groups.get(i - 1);
                final BigDecimal delta;
                
                // destination = previousGroup.max + gapOrOverlapSize
                // source = group.min
                // source + delta = destination
                // delta = destination - source
                delta = previousGroup.max()[0].add(gapOrOverlapSize).subtract(group.min()[0]);
                
                // Add delta to the maximum, because it was moved.
                group.max()[0] = group.max()[0].add(delta);
                
                vm.backupSelection();
                vm.clearSelection2();
                final Set<Integer> selectedLineNumbers = vm.addToSelection(group.group());
                final BigDecimal deltaX = distributeOnX ? delta : BigDecimal.ZERO;
                final BigDecimal deltaY = distributeOnY ? delta : BigDecimal.ZERO;
                final BigDecimal deltaZ = distributeOnZ ? delta : BigDecimal.ZERO;
                
                final Matrix m = View.ACCURATE_ID.translate(new BigDecimal[]{deltaX, deltaY, deltaZ});
                vm.transformSelection(m, null, MiscToggleToolItem.isMovingAdjacentData(), false);
                vm.restoreSelection();
                for (int number : selectedLineNumbers) {
                    vm.addTextLineToSelection(number); 
                }
            }
            
            finishModification(vm, groups);
        }
    }
    
    private static void calculateMinMaxAvgForGroups(final List<SelectionGroup> groups, final VertexManager vm, final AlignAndDistribute axis) {
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
            if (size > 0 && min != null && max != null) {
                avg = min.add(max).divide(BigDecimal.TWO, Threshold.MC);
                selectionGroup.min()[0] = min;
                selectionGroup.max()[0] = max;
                selectionGroup.avg()[0] = avg;
                
                NLogger.debug(AlignAndDistribute.class, "Min : {0} of group {1}", min, selectionGroup); //$NON-NLS-1$
                NLogger.debug(AlignAndDistribute.class, "Max : {0}", max); //$NON-NLS-1$
                NLogger.debug(AlignAndDistribute.class, "Avg : {0}", avg); //$NON-NLS-1$
            } else {
                // Remove this group because it does not contain a vertex (should not happen)
                groupsToDelete.add(selectionGroup);
                NLogger.debug(AlignAndDistribute.class, "Group was deleted : {0}", selectionGroup); //$NON-NLS-1$
            }
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
    
    private static List<SelectionGroup> calculateSelectionGroups(final VertexManager vm) {
        final List<SelectionGroup> result = new ArrayList<>();
        // If "Move Adjacent Data" is on, then adjacent selected data (n) should be considered as a group.
        // This will cause nearly O(n * log n) complexity, because of the TreeMap "vgroups", which indicates if a vertex is already part of a group.
        // Subfile content should be ignored, since it can't be transformed (only the subfile as a whole can be moved).
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
            final Map<Vertex, Set<GData>> vgroups = new TreeMap<>();
            for (GData data : selection) {
                final Set<VertexInfo> vis = vm.getLineLinkedToVertices().getOrDefault(data, Set.of());
                boolean needsGroup = true;
                for (VertexInfo vi : vis) {
                    // Don't look at condline control points
                    if (data.type() == 5 && vi.getPosition() > 1) continue;
                    
                    final Vertex v = vi.getVertex();
                    if (vgroups.containsKey(v)) {
                        final Set<GData> group = vgroups.get(v);
                        group.add(data);
                        for (VertexInfo vi2 : vis) {
                            // Don't look at condline control points
                            if (data.type() == 5 && vi2.getPosition() > 1) continue;
                            final Vertex v2 = vi2.getVertex();
                            vgroups.put(v2, group);
                        }
                        
                        needsGroup = false;
                        break;
                    }
                }
                
                if (needsGroup) {
                    final Set<GData> group = new HashSet<>();
                    group.add(data);
                    
                    for (VertexInfo vi2 : vis) {
                        // Don't look at condline control points
                        if (data.type() == 5 && vi2.getPosition() > 1) continue;
                        final Vertex v2 = vi2.getVertex();
                        vgroups.put(v2, group);
                    }
                }
            }
            
            // We determined all adjacent groups and data without adjacency stands for itself
            result.addAll(new HashSet<>(vgroups.values()).stream().map(ArrayList::new).map(AlignAndDistribute::asSelectionGroup).toList());
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
