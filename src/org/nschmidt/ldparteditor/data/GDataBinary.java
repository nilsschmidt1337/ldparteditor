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

import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeHashMap;
import org.nschmidt.ldparteditor.helper.math.ThreadsafeSortedMap;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 * Holds binary base64 encoded data
 */
public final class GDataBinary extends GData {

    /** 4 chars = 3 bytes  => 64.000 chars = 48.000 bytes */
    private static final int MAX_LENGTH_OF_BASE64_STRING = 64_000;
    
    private final DatFile df; 
    
    public GDataBinary(String text, DatFile df, GData1 parent) {
        super(parent);
        this.df = df;
        this.text = text;
        this.df.getBinaryData().addData(this);
    }
    
    public byte[] loadBinary() {
        final Pattern whitespace = Pattern.compile("\\s+"); //$NON-NLS-1$
        final StringBuilder base64Sb = new StringBuilder();
        
        GData gd = this.next;
        while (gd != null && base64Sb.length() <= MAX_LENGTH_OF_BASE64_STRING) {
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
            if (encodedString.length() <= MAX_LENGTH_OF_BASE64_STRING) {
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
        int state = 0;
        int length = 0;
        int dataLength = 0;
        for (byte b : data) {
            if (dataLength > 0) {
                break;
            }
            
            // Encoders and decoders must treat the codes as fixed binary values,
            // not character strings.
            switch (b) {
            case 73: { // I
                if (state == 0) {
                    state = 1;
                } else {
                    state = 0;
                }
                
                break;
            }
            case 69: { // E
                if (state == 1) {
                    state = 2;
                } else {
                    state = 0;
                }
                
                break;
            }
            case 78: { // N
                if (state == 2) {
                    state = 3;
                } else {
                    state = 0;
                }
                
                break;
            }
            case 68: { // D
                if (state == 3) {
                    // + 4 Byte CRC for IEND
                    dataLength = Math.min(length + 5, data.length);
                } else {
                    state = 0;
                }
                
                break;
            }
            default:
                // Do nothing.
            }
            
            length++;
        }
        
        final byte[] resultData = new byte[dataLength];
        System.arraycopy(data, 0, resultData, 0, dataLength);
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
