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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

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
        for (GData1 rect : rectPrims) {
            snap(rect);
        }
    }

    private void snap(GData1 rect) {
        // FIXME Needs implementation for issue #230!
                
        // 1. Took all vertices in order from the rectangle-primitive
        
        Set<VertexInfo> vis = lineLinkedToVertices.get(rect);
        if (vis == null) return;
        for (VertexInfo vi : vis) {
            if (vi.linkedData.type() == 4) {
                NLogger.debug(getClass(), vi.position);
            }
        }
    }
}
