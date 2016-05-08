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

import org.lwjgl.util.vector.Vector3f;
import org.nschmidt.ldparteditor.helpers.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.logger.NLogger;

public class VM25RectangleSnap extends VM24MeshReducer {

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
        clearSelection();
        for (GData1 rect : rectPrims) {
            snap(rect);
        }
        if (!rectPrims.isEmpty()) {
            setModified(true, true);
        }
    }

    private void snap(GData1 rect) {
        // FIXME Needs implementation for issue #230!
                
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
        // FIXME Needs implementation for issue #230!
        return verts;
    }
    
    private void rectify(Vertex[] loop, GData1 g) {
        // FIXME Needs implementation for issue #230!
        
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
