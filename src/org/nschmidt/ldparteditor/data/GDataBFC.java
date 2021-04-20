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

import java.util.Map;

import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeSortedMap;

/**
 * @author nils
 *
 */
public final class GDataBFC extends GData {

    final BFC type;

    public BFC getType() {
        return type;
    }

    public GDataBFC(BFC type, GData1 parent) {
        super(parent);
        this.type = type;
    }

    @Override
    public void drawGL20(Composite3D c3d) {
        // Nothing to do, since BFC is turned off
        // Except clipping check
        switch (type) {
        case CCW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CCW;
            break;
        case CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            break;
        case CW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CW;
            break;
        default:
            break;
        }
    }

    @Override
    public void drawGL20RandomColours(Composite3D c3d) {
        // Nothing to do, since BFC is turned off
        // Except clipping check
        switch (type) {
        case CCW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CCW;
            break;
        case CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            break;
        case CW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CW;
            break;
        default:
            break;
        }
    }

    @Override
    public void drawGL20BFC(Composite3D c3d) {
        switch (type) {
        case CCW:
            GData.localWinding = BFC.CCW;
            break;
        case CCW_CLIP:
            GData.localWinding = BFC.CCW;
            break;
        case CW:
            GData.localWinding = BFC.CW;
            break;
        case CW_CLIP:
            GData.localWinding = BFC.CW;
            break;
        case INVERTNEXT:
            boolean validState = false;
            GData g = next;
            while (g != null && g.type() < 2) {
                validState = g.type() == 1 && g.visible;
                if (validState || !g.toString().trim().isEmpty()) {
                    break;
                }
                g = g.next;
            }
            if (validState || (g == null && c3d.getRenderMode() == 5)) {
                GData.globalInvertNext = !GData.globalInvertNext;
                GData.globalInvertNextFound = true;
            }
            break;
        case NOCERTIFY:
            GData.localWinding = BFC.NOCERTIFY;
            break;
        case NOCLIP:
            if (GData.accumClip == 0)
                GData.accumClip = 1;
            break;
        default:
            break;
        }
    }

    @Override
    public void drawGL20BFCuncertified(Composite3D c3d) {
        // Nothing to do while the part is uncertified.
    }

    @Override
    public void drawGL20BFCbackOnly(Composite3D c3d) {
        drawGL20BFC(c3d);
    }

    @Override
    public void drawGL20BFCcolour(Composite3D c3d) {
        drawGL20BFC(c3d);
    }

    @Override
    public void drawGL20BFCtextured(Composite3D c3d) {
        drawGL20BFC(c3d);
    }

    @Override
    public void drawGL20WhileAddCondlines(Composite3D c3d) {
        drawGL20BFC(c3d);
    }

    @Override
    public void drawGL20CoplanarityHeatmap(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public void drawGL20Wireframe(Composite3D c3d) {
        drawGL20(c3d);
    }

    @Override
    public int type() {
        return 6;
    }

    @Override
    String getNiceString() {
        switch (type) {
        case CLIP:
            return "0 BFC CLIP"; //$NON-NLS-1$
        case CCW_CLIP:
            return "0 BFC CERTIFY CCW"; //$NON-NLS-1$
        case CCW:
            return "0 BFC CLIP CCW"; //$NON-NLS-1$
        case CW_CLIP:
            return "0 BFC CERTIFY CW"; //$NON-NLS-1$
        case CW:
            return "0 BFC CLIP CW"; //$NON-NLS-1$
        case INVERTNEXT:
            return "0 BFC INVERTNEXT"; //$NON-NLS-1$
        case NOCERTIFY:
            return "0 BFC NOCERTIFY"; //$NON-NLS-1$
        case NOCLIP:
            return "0 BFC NOCLIP"; //$NON-NLS-1$
        default:
            break;
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public String inlinedString(BFC bfc, GColour colour) {
        return getNiceString();
    }

    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        return getNiceString();
    }

    @Override
    public void getBFCorientationMap(Map<GData,BFC> map) {
        switch (type) {
        case CCW:
            GData.localWinding = BFC.CCW;
            break;
        case CCW_CLIP:
            GData.localWinding = BFC.CCW;
            break;
        case CW:
            GData.localWinding = BFC.CW;
            break;
        case CW_CLIP:
            GData.localWinding = BFC.CW;
            break;
        case INVERTNEXT:
            boolean validState = false;
            GData g = next;
            while (g != null && g.type() < 2) {
                validState = g.type() == 1 && g.visible;
                if (validState || !g.toString().trim().isEmpty()) {
                    break;
                }
                g = g.next;
            }
            if (validState) {
                GData.globalInvertNext = !GData.globalInvertNext;
                GData.globalInvertNextFound = true;
            }
            break;
        case NOCERTIFY:
            GData.localWinding = BFC.NOCERTIFY;
            break;
        case NOCLIP:
            if (GData.accumClip == 0)
                GData.accumClip = 1;
            break;
        default:
            break;
        }
    }

    @Override
    public void getBFCorientationMapNOCERTIFY(Map<GData, BFC> map) {
        getBFCorientationMap(map);
    }

    @Override
    public void getBFCorientationMapNOCLIP(Map<GData, BFC> map) {
        switch (type) {
        case CCW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CCW;
            break;
        case CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            break;
        case CW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CW;
            break;
        default:
            break;
        }
    }

    @Override
    public void getVertexNormalMap(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        switch (type) {
        case CCW:
            state.localWinding = BFC.CCW;
            break;
        case CCW_CLIP:
            state.localWinding = BFC.CCW;
            break;
        case CW:
            state.localWinding = BFC.CW;
            break;
        case CW_CLIP:
            state.localWinding = BFC.CW;
            break;
        case INVERTNEXT:
            boolean validState = false;
            GData g = next;
            while (g != null && g.type() < 2) {
                validState = g.type() == 1 && g.visible;
                if (validState || !g.toString().trim().isEmpty()) {
                    break;
                }
                g = g.next;
            }
            if (validState) {
                state.globalInvertNext = !state.globalInvertNext;
                state.globalInvertNextFound = true;
            }
            break;
        case NOCERTIFY:
            state.localWinding = BFC.NOCERTIFY;
            break;
        case NOCLIP:
            if (state.accumClip == 0)
                state.accumClip = 1;
            break;
        default:
            break;
        }
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        getVertexNormalMap(state, vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
    }

    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        switch (type) {
        case CCW_CLIP:
            if (state.accumClip == 1)
                state.accumClip = 0;
            state.localWinding = BFC.CCW;
            break;
        case CLIP:
            if (state.accumClip == 1)
                state.accumClip = 0;
            break;
        case CW_CLIP:
            if (state.accumClip == 1)
                state.accumClip = 0;
            state.localWinding = BFC.CW;
            break;
        default:
            break;
        }
    }
}
