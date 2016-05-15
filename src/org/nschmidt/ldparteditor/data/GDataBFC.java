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

import java.util.HashMap;

import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helpers.math.ThreadsafeTreeMap;

/**
 * @author nils
 *
 */
public final class GDataBFC extends GData {

    final byte type;

    public byte getType() {
        return type;
    }

    public GDataBFC(byte type) {
        this.type = type;
    }

    @Override
    public void draw(Composite3D c3d) {
        // Nothing to do, since BFC is turned off
        // Except clipping check
        switch (type) {
        case BFC.CCW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CCW;
            break;
        case BFC.CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            break;
        case BFC.CW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CW;
            break;
        default:
            break;
        }
    }

    @Override
    public void drawRandomColours(Composite3D c3d) {
        // Nothing to do, since BFC is turned off
        // Except clipping check
        switch (type) {
        case BFC.CCW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CCW;
            break;
        case BFC.CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            break;
        case BFC.CW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CW;
            break;
        default:
            break;
        }
    }

    @Override
    public void drawBFC(Composite3D c3d) {
        switch (type) {
        case BFC.CCW:
            GData.localWinding = BFC.CCW;
            break;
        case BFC.CCW_CLIP:
            GData.localWinding = BFC.CCW;
            break;
        case BFC.CW:
            GData.localWinding = BFC.CW;
            break;
        case BFC.CW_CLIP:
            GData.localWinding = BFC.CW;
            break;
        case BFC.INVERTNEXT:
            GData.globalInvertNext = !GData.globalInvertNext;
            GData.globalInvertNextFound = true;
            break;
        case BFC.NOCERTIFY:
            GData.localWinding = BFC.NOCERTIFY;
            break;
        case BFC.NOCLIP:
            if (GData.accumClip == 0)
                GData.accumClip = 1;
            break;
        default:
            break;
        }
    }

    @Override
    public void drawBFCuncertified(Composite3D c3d) {}

    @Override
    public void drawBFC_backOnly(Composite3D c3d) {
        drawBFC(null);
    }

    @Override
    public void drawBFC_Colour(Composite3D c3d) {
        drawBFC(null);
    }

    @Override
    public void drawBFC_Textured(Composite3D c3d) {
        drawBFC(null);
    }

    @Override
    public void drawWhileAddCondlines(Composite3D c3d) {
        drawBFC(null);
    }

    @Override
    public int type() {
        return 6;
    }

    @Override
    String getNiceString() {
        switch (type) {
        case BFC.CLIP:
            return "0 BFC CLIP"; //$NON-NLS-1$
        case BFC.CCW_CLIP:
            return "0 BFC CERTIFY CCW"; //$NON-NLS-1$
        case BFC.CCW:
            return "0 BFC CLIP CCW"; //$NON-NLS-1$
        case BFC.CW_CLIP:
            return "0 BFC CERTIFY CW"; //$NON-NLS-1$
        case BFC.CW:
            return "0 BFC CLIP CW"; //$NON-NLS-1$
        case BFC.INVERTNEXT:
            return "0 BFC INVERTNEXT"; //$NON-NLS-1$
        case BFC.NOCERTIFY:
            return "0 BFC NOCERTIFY"; //$NON-NLS-1$
        case BFC.NOCLIP:
            return "0 BFC NOCLIP"; //$NON-NLS-1$
        default:
            break;
        }
        return ""; //$NON-NLS-1$
    }

    @Override
    public String inlinedString(byte bfc, GColour colour) {
        return getNiceString();
    }

    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        return getNiceString();
    }

    @Override
    public void getBFCorientationMap(HashMap<GData, Byte> map) {
        switch (type) {
        case BFC.CCW:
            GData.localWinding = BFC.CCW;
            break;
        case BFC.CCW_CLIP:
            GData.localWinding = BFC.CCW;
            break;
        case BFC.CW:
            GData.localWinding = BFC.CW;
            break;
        case BFC.CW_CLIP:
            GData.localWinding = BFC.CW;
            break;
        case BFC.INVERTNEXT:
            GData.globalInvertNext = !GData.globalInvertNext;
            GData.globalInvertNextFound = true;
            break;
        case BFC.NOCERTIFY:
            GData.localWinding = BFC.NOCERTIFY;
            break;
        case BFC.NOCLIP:
            if (GData.accumClip == 0)
                GData.accumClip = 1;
            break;
        default:
            break;
        }
    }

    @Override
    public void getBFCorientationMapNOCERTIFY(HashMap<GData, Byte> map) {
        getBFCorientationMap(map);
    }

    @Override
    public void getBFCorientationMapNOCLIP(HashMap<GData, Byte> map) {
        switch (type) {
        case BFC.CCW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CCW;
            break;
        case BFC.CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            break;
        case BFC.CW_CLIP:
            if (GData.accumClip == 1)
                GData.accumClip = 0;
            GData.localWinding = BFC.CW;
            break;
        default:
            break;
        }
    }

    @Override
    public void getVertexNormalMap(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        switch (type) {
        case BFC.CCW:
            state.localWinding = BFC.CCW;
            break;
        case BFC.CCW_CLIP:
            state.localWinding = BFC.CCW;
            break;
        case BFC.CW:
            state.localWinding = BFC.CW;
            break;
        case BFC.CW_CLIP:
            state.localWinding = BFC.CW;
            break;
        case BFC.INVERTNEXT:
            state.globalInvertNext = !state.globalInvertNext;
            state.globalInvertNextFound = true;
            break;
        case BFC.NOCERTIFY:
            state.localWinding = BFC.NOCERTIFY;
            break;
        case BFC.NOCLIP:
            if (state.accumClip == 0)
                state.accumClip = 1;
            break;
        default:
            break;
        }
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        getVertexNormalMap(state, vertexLinkedToNormalCACHE, dataLinkedToNormalCACHE, vm);
    }

    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeTreeMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        switch (type) {
        case BFC.CCW_CLIP:
            if (state.accumClip == 1)
                state.accumClip = 0;
            state.localWinding = BFC.CCW;
            break;
        case BFC.CLIP:
            if (state.accumClip == 1)
                state.accumClip = 0;
            break;
        case BFC.CW_CLIP:
            if (state.accumClip == 1)
                state.accumClip = 0;
            state.localWinding = BFC.CW;
            break;
        default:
            break;
        }
    }

}
