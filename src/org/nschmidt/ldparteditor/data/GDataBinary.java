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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

import de.matthiasmann.twl.util.PNGSizeDeterminer;

/**
 * Holds binary base64 encoded data
 */
public final class GDataBinary extends GData {

    private final DatFile df; 
    
    public GDataBinary(String text, DatFile df, GData1 parent) {
        super(parent);
        this.df = df;
        this.text = text;
        this.df.getBinaryData().addData(this);
    }
    
    public byte[] loadBinary() {
        // 4 chars = 3 bytes  => 64.000 chars = 48.000 bytes
        final int maxLengthOfBase64String = WorkbenchManager.getUserSettingState().getDataFileSizeLimit() * 1_000 / 3 * 4;
        final Pattern whitespace = Pattern.compile("\\s+"); //$NON-NLS-1$
        final StringBuilder base64Sb = new StringBuilder();
        
        GData gd = this.next;
        while (gd != null && base64Sb.length() <= maxLengthOfBase64String) {
            final String line = whitespace.matcher(gd.toString()).replaceAll(" ").trim(); //$NON-NLS-1$
            if (line.startsWith("0 !: ")) { //$NON-NLS-1$
                final String encodedSubstring = line.substring(5);
                base64Sb.append(encodedSubstring);
            } else if (line.length() > 0) {
                break;
            }
            
            gd = gd.next;
        }
        
        final String encodedString = base64Sb.toString();
        
        try {
            // Don't allow more than a constant amount of chars
            if (encodedString.length() <= maxLengthOfBase64String) {
                final byte[] preFilteredData = Base64.getDecoder().decode(encodedString);
                return filterMaliciousContent(preFilteredData);
            }
        } catch (IllegalArgumentException iae) {
            NLogger.debug(GDataBinary.class, iae);
        }
        
        return new byte[0];
    }
    
    /**
     * Malicious content can be attached to the end of the file after the IEND tag which typically marks the
     * end of the image file. We don't want to read or store this! 
     * @param data the byte array to filter
     * @return the filtered array
     */
    private byte[] filterMaliciousContent(byte[] data) {
        
        int dataLength = 0;
        try (InputStream in = new ByteArrayInputStream(data)) {
            dataLength = new PNGSizeDeterminer(in).size();
        } catch (IOException ioe) {
            NLogger.debug(GDataBinary.class, ioe);
            return new byte[0];
        }
        
        final byte[] resultData = new byte[dataLength + 12];
        System.arraycopy(data, 0, resultData, 0, dataLength);
        
        // Add IEND tag (12 Bytes)
        resultData[dataLength + 0] = 0x00;
        resultData[dataLength + 1] = 0x00;
        resultData[dataLength + 2] = 0x00;
        resultData[dataLength + 3] = 0x00;
        
        resultData[dataLength + 4] = 0x49;
        resultData[dataLength + 5] = 0x45;
        resultData[dataLength + 6] = 0x4E;
        resultData[dataLength + 7] = 0x44;
        
        resultData[dataLength + 8] = (byte) 0xAE;
        resultData[dataLength + 9] = 0x42;
        resultData[dataLength + 10] = 0x60;
        resultData[dataLength + 11] = (byte) 0x82;
        return resultData;
    }

    @Override
    public void drawGL20(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20RandomColours(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFC(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFCuncertified(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFCbackOnly(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFCcolour(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20WhileAddCondlines(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20CoplanarityHeatmap(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20Wireframe(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public void drawGL20BFCtextured(Composite3D c3d) {
        // Implementation is not required.
    }

    @Override
    public int type() {
        return 11;
    }

    @Override
    String getNiceString() {
        return text;
    }

    @Override
    public String inlinedString(BFC bfc, GColour colour) {
        return text;
    }

    @Override
    public String transformAndColourReplace(String colour, Matrix matrix) {
        return text;
    }

    @Override
    public void getBFCorientationMap(Map<GData,BFC> map) {
        // Implementation is not required.
    }

    @Override
    public void getBFCorientationMapNOCERTIFY(Map<GData, BFC> map) {
        // Implementation is not required.
    }

    @Override
    public void getBFCorientationMapNOCLIP(Map<GData, BFC> map) {
        // Implementation is not required.
    }

    @Override
    public void getVertexNormalMap(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        // Implementation is not required.
    }

    @Override
    public void getVertexNormalMapNOCERTIFY(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        // Implementation is not required.
    }

    @Override
    public void getVertexNormalMapNOCLIP(GDataState state, ThreadsafeSortedMap<Vertex, float[]> vertexLinkedToNormalCACHE, ThreadsafeHashMap<GData, float[]> dataLinkedToNormalCACHE, VM00Base vm) {
        // Implementation is not required.
    }
}
