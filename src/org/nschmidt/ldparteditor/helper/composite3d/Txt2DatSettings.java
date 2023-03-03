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
package org.nschmidt.ldparteditor.helper.composite3d;

import java.math.BigDecimal;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

public class Txt2DatSettings {

    private BigDecimal flatness = new BigDecimal("0.2"); //$NON-NLS-1$
    private BigDecimal fontHeight = new BigDecimal("100"); //$NON-NLS-1$
    private BigDecimal deltaAngle = new BigDecimal("12.5"); //$NON-NLS-1$
    private FontData fontData = null;
    private String text = "";  //$NON-NLS-1$
    private RGB rgb = null;
    private int mode = 0;
    public BigDecimal getFlatness() {
        return flatness;
    }
    public void setFlatness(BigDecimal flatness) {
        this.flatness = flatness;
    }
    public BigDecimal getFontHeight() {
        return fontHeight;
    }
    public void setFontHeight(BigDecimal fontHeight) {
        this.fontHeight = fontHeight;
    }
    public BigDecimal getDeltaAngle() {
        return deltaAngle;
    }
    public void setDeltaAngle(BigDecimal deltaAngle) {
        this.deltaAngle = deltaAngle;
    }
    public FontData getFontData() {
        return fontData;
    }
    public void setFontData(FontData fontData) {
        this.fontData = fontData;
    }
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public RGB getRGB() {
        return rgb;
    }
    public void setRGB(RGB rgb) {
        this.rgb = rgb;
    }
    public int getMode() {
        return mode;
    }
    public void setMode(int mode) {
        this.mode = mode;
    }
}
