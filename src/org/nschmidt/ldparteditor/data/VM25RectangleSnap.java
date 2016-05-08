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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.helpers.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.logger.NLogger;

public class VM25RectangleSnap extends VM24MeshReducer {

    private TreeSet<Vertex> selectedVerticesBackup = new TreeSet<Vertex>();
    private final Vertex[] NO_SOLUTION = new Vertex[]{};
    
    
    protected VM25RectangleSnap(DatFile linkedDatFile) {
        super(linkedDatFile);
    }
    
    public void snapRectanglePrimitives() {
        ArrayList<GData1> rectPrims = new ArrayList<GData1>();
        final HashSet<String> RECT_NAMES = new HashSet<String>();        
        RECT_NAMES.add("rect1.dat"); //$NON-NLS-1$
        RECT_NAMES.add("rect2a.dat"); //$NON-NLS-1$
        RECT_NAMES.add("rect2p.dat"); //$NON-NLS-1$
        RECT_NAMES.add("rect3.dat"); //$NON-NLS-1$
        RECT_NAMES.add("rect.dat"); //$NON-NLS-1$
        for (GData1 g : selectedSubfiles) {
            if (RECT_NAMES.contains(g.shortName.toLowerCase(Locale.ENGLISH))) {
                rectPrims.add(g);
            }
        }
        for (GData1 rect : rectPrims) {
            Set<VertexInfo> vis = lineLinkedToVertices.get(rect);
            if (vis != null) {
                for (VertexInfo vi : vis) {
                    selectedVertices.remove(vi.vertex);
                }
            }
        }
        selectedVerticesBackup.addAll(selectedVertices);
        clearSelection();
        for (GData1 rect : rectPrims) {
            snap(rect);
        }
        if (!rectPrims.isEmpty()) {
            setModified(true, true);
        }
    }

    private void snap(GData1 rect) {
                
        // 1. Took all vertices in order from the rectangle-primitive
        
        Vertex[] verts = null;
        {
            Set<VertexInfo> vis = lineLinkedToVertices.get(rect);
            if (vis == null) return;        
            for (VertexInfo vi : vis) {
                if (vi.linkedData.type() == 4) {
                    NLogger.debug(getClass(), vi.position);
                    verts = quads.get(vi.linkedData);
                    break;
                }
            }
        }
        if (verts == null) return;
        
        // 2. Get a possible (and valid) new 4-vertex loop from the vertices of the rectangle
        
        Vertex[] loop = getLoop(verts);
        if (loop == null) return;
        
        // 3. Rectify the loop (with rectangle-primitives), delete the old rectangle primitive  
        rectify(loop, rect);
    }   

    private Vertex[] getLoop(Vertex[] verts) {
        Vertex[][] loops = new Vertex[4][];
        for (int i = 0; i < 4; i++) {
            loops[i] = getLoop(verts[i], verts);
        }
        {
            float minDist = Float.MAX_VALUE;
            for (int i = 0; i < 4; i++) {
                if (loops[i] != null && loops[i][0].x < minDist) minDist = loops[i][0].x;
            }
            for (int i = 0; i < 4; i++) {
                if (loops[i] != null && loops[i][0].x == minDist){
                    verts[0] = loops[i][1];
                    verts[1] = loops[i][2];
                    verts[2] = loops[i][3];
                    verts[3] = loops[i][4];
                    break;
                }
            }
        }        
        return verts;
    }
    
    private Vertex[] getLoop(Vertex vert, Vertex[] verts) {
        // FIXME Needs implementation for issue #230!
        Vertex[] result = new Vertex[5];
        TreeSet<Vertex> validVertices = new TreeSet<Vertex>();
        if (selectedVerticesBackup.isEmpty()) {
            validVertices.addAll(getVertices());    
        } else {
            validVertices.addAll(selectedVerticesBackup);
        }
        for (int i = 0; i < 4; i++) {
            validVertices.remove(verts[i]);
        }
        boolean isConnectedToModel = getLinkedSurfaces(vert).size() > 1;
        BigDecimal minDistance = new BigDecimal("10000000000000"); //$NON-NLS-1$
        
        // If the vertex is not connected to other surfaces, search for the closest vertex
        if (!isConnectedToModel) {      
            Vertex vert2 = vert;
            for (Vertex v : validVertices) {
                Vector3d v1 = new Vector3d(v);
                Vector3d v2 = new Vector3d(vert);
                BigDecimal distance = Vector3d.distSquare(v1, v2);
                if (distance.compareTo(minDistance) < 0) {                    
                    minDistance = distance;
                    vert2 = v;
                }
            }
            vert = vert2;
        }
        
        Vertex[] loop = getLoop(vert, validVertices);
        if (loop != null) {
            result[0] = new Vertex(minDistance, BigDecimal.ZERO, BigDecimal.ZERO);
            result[1] = loop[0];
            result[2] = loop[1];
            result[3] = loop[2];
            result[4] = loop[3];
        }
        return result;
    }
    
    private Vertex[] getLoop(Vertex vert, TreeSet<Vertex> validVertices) {
        return getLoop(vert, new Vertex[4], 0, validVertices);
    }

    private Vertex[] getLoop(Vertex vert, Vertex[] verts, int depth,
            TreeSet<Vertex> validVertices) {
                       
        if (depth > 3) {
            return null;
        }
               
        Vertex backup = verts[depth];
        verts[depth] = vert;
        
        NLogger.debug(getClass(), "Depth: " + depth + " " + verts[0] + " " + verts[1] + " " + verts[2] + " " + verts[3]); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$        
                
        TreeSet<Vertex> surfVerts = new TreeSet<Vertex>();
        {
            HashSet<GData> surfs = getLinkedSurfaces(vert);
            for (GData g : surfs) {
                int type = g.type();
                if (type == 3) {
                    Vertex[] t = triangles.get(g);
                    if (t == null) continue;
                    surfVerts.add(t[0]);
                    surfVerts.add(t[1]);
                    surfVerts.add(t[2]);
                } else if (type == 4) {
                    Vertex[] t = quads.get(g);
                    if (t == null) continue;
                    surfVerts.add(t[0]);
                    surfVerts.add(t[1]);
                    surfVerts.add(t[2]);
                    surfVerts.add(t[3]);
                }
            }
        }
        
        boolean finishedLoopSearch = false; 
        if (depth == 3) {
            finishedLoopSearch = surfVerts.contains(verts[0]);
        }                
        
        for (int i = 0; i < (depth + 1); i++) {
            surfVerts.remove(verts[i]);   
        }
        boolean foundSolution = false;
        
        if (finishedLoopSearch) {
            
            HashSet<GData> s1 = getLinkedSurfaces(verts[0]);
            HashSet<GData> s2 = getLinkedSurfaces(verts[1]);
            HashSet<GData> s3 = getLinkedSurfaces(verts[2]);
            HashSet<GData> s4 = getLinkedSurfaces(verts[3]);
            
            s1.retainAll(s2);
            s1.retainAll(s3);
            s1.retainAll(s4);
            
            if (!s1.isEmpty()) {
                verts[depth] = backup;
                return NO_SOLUTION;
            }
            
        } else {
            
            for (Vertex v : surfVerts) {
                Vertex[] solution = getLoop(v, verts, (depth + 1), validVertices);
                if (solution != NO_SOLUTION) {
                    foundSolution = true;
                }
            }
            if (depth > 0 && !foundSolution) {
                verts[depth] = backup;
            }
        }
        
        if (verts[0] != null && verts[1] != null && verts[2] != null && verts[3] != null) {
            return verts;
        } else {
            return NO_SOLUTION;           
        }       
    }

    private void rectify(Vertex[] loop, GData1 g) {
        final Vector3f[] normals = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
        {
            Vertex v1 = loop[0];
            Vertex v2 = loop[1];
            Vertex v3 = loop[2];
            Vertex v4 = loop[3];
            final Vector3f[] lineVectors = new Vector3f[] { new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f() };
            Vector3f.sub(new Vector3f(v2.x, v2.y, v2.z), new Vector3f(v1.x, v1.y, v1.z), lineVectors[0]);
            Vector3f.sub(new Vector3f(v3.x, v3.y, v3.z), new Vector3f(v2.x, v2.y, v2.z), lineVectors[1]);
            Vector3f.sub(new Vector3f(v4.x, v4.y, v4.z), new Vector3f(v3.x, v3.y, v3.z), lineVectors[2]);
            Vector3f.sub(new Vector3f(v1.x, v1.y, v1.z), new Vector3f(v4.x, v4.y, v4.z), lineVectors[3]);
            Vector3f.cross(lineVectors[0], lineVectors[1], normals[0]);
            Vector3f.cross(lineVectors[1], lineVectors[2], normals[1]);
            Vector3f.cross(lineVectors[2], lineVectors[3], normals[2]);
            Vector3f.cross(lineVectors[3], lineVectors[0], normals[3]);
        }
        Vector3f normal = new Vector3f();
        for (int i = 0; i < 4; i++) {
            Vector3f.add(normals[i], normal, normal);
        }
        GData4 quad = new GData4(
                g.colourNumber, g.r, g.g, g.b, g.a, 
                loop[0].X, loop[0].Y, loop[0].Z, 
                loop[1].X, loop[1].Y, loop[1].Z, 
                loop[2].X, loop[2].Y, loop[2].Z, 
                loop[3].X, loop[3].Y, loop[3].Z, 
                new Vector3d(new BigDecimal(normal.x), new BigDecimal(normal.y), new BigDecimal(normal.z)), 
                g.parent, linkedDatFile);
        linker(g, quad);
        selectedData.add(quad);
        selectedQuads.add(quad);
        RectifierSettings rs = new RectifierSettings();
        rs.setScope(1);        
        rectify(rs, false, false);
        selectedData.clear();
        selectedQuads.clear();
    }
}
