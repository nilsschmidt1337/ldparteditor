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

import org.nschmidt.ldparteditor.helpers.math.MathHelper;

/**
 * @author nils
 *
 */
public class GColour implements Serializable {

    private static final long serialVersionUID = 1L;

    private int colourNumber;
    private GColourType type;
    private float r;
    private float g;
    private float b;
    private float a;

    public GColour() {

    }

    public GColour(int colourNumber, float r, float g, float b, float a) {
        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public GColour(int colourNumber, float r, float g, float b, float a, GColourType type) {
        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        this.type = type;
    }

    @Override
    public GColour clone() {
        return new GColour(colourNumber, r, g, b, a, GColourType.clone(type));
    };

    public int getColourNumber() {
        return colourNumber;
    }

    public void setColourNumber(int colourNumber) {
        this.colourNumber = colourNumber;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getB() {
        return b;
    }

    public void setB(float b) {
        this.b = b;
    }

    public float getA() {
        return a;
    }

    public void setA(float a) {
        this.a = a;
    }

    public void set(GColour colour) {
        this.colourNumber = colour.colourNumber;
        this.r = colour.r;
        this.g = colour.g;
        this.b = colour.b;
        this.a = colour.a;
        this.type = GColourType.clone(colour.type);
    }

    public void set(int colourNumber, float r, float g, float b, float a) {
        this.colourNumber = colourNumber;
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Float.floatToIntBits(a);
        result = prime * result + Float.floatToIntBits(b);
        result = prime * result + colourNumber;
        result = prime * result + Float.floatToIntBits(g);
        result = prime * result + Float.floatToIntBits(r);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GColour other = (GColour) obj;
        if (Float.floatToIntBits(a) != Float.floatToIntBits(other.a))
            return false;
        if (Float.floatToIntBits(b) != Float.floatToIntBits(other.b))
            return false;
        if (colourNumber != other.colourNumber)
            return false;
        if (Float.floatToIntBits(g) != Float.floatToIntBits(other.g))
            return false;
        if (Float.floatToIntBits(r) != Float.floatToIntBits(other.r))
            return false;
        return true;
    }

    public GColourType getType() {
        return type;
    }

    public void setType(GColourType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder colourBuilder = new StringBuilder();
        if (colourNumber == -1) {
            colourBuilder.append("0x2"); //$NON-NLS-1$
            colourBuilder.append(MathHelper.toHex((int) (255f * r)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * g)).toUpperCase());
            colourBuilder.append(MathHelper.toHex((int) (255f * b)).toUpperCase());
        } else {
            colourBuilder.append(colourNumber);
        }
        return colourBuilder.toString();
    }
}
