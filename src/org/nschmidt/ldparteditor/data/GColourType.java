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

import java.io.Serializable;

import org.nschmidt.ldparteditor.data.colour.GCChrome;
import org.nschmidt.ldparteditor.data.colour.GCDithered;
import org.nschmidt.ldparteditor.data.colour.GCGlitter;
import org.nschmidt.ldparteditor.data.colour.GCMatteMetal;
import org.nschmidt.ldparteditor.data.colour.GCMetal;
import org.nschmidt.ldparteditor.data.colour.GCPearl;
import org.nschmidt.ldparteditor.data.colour.GCRubber;
import org.nschmidt.ldparteditor.data.colour.GCSpeckle;
import org.nschmidt.ldparteditor.data.colour.GCType;


/**
 * @author nils
 *
 */
public abstract class GColourType implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract GCType type();

    public static GColourType clone(GColourType type) {
        if (type != null) {
            switch (type.type()) {
            case CHROME:
                return new GCChrome();
            case DITHERED:
                return new GCDithered();
            case PEARL:
                return new GCPearl();
            case MATTE_METALLIC:
                return new GCMatteMetal();
            case METAL:
                return new GCMetal();
            case RUBBER:
                return new GCRubber();
            case GLITTER:
                GCGlitter g = (GCGlitter) type;
                return new GCGlitter(g.getR(), g.getG(), g.getB(), g.getFraction(), g.getMinSize(), g.getMaxSize());
            case SPECKLE:
                GCSpeckle s = (GCSpeckle) type;
                return new GCSpeckle(s.getR(), s.getG(), s.getB(), s.getFraction(), s.getMinSize(), s.getMaxSize());
            }
        }
        return null;
    }

}
