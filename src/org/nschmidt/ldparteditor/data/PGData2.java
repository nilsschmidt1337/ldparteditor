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

import org.lwjgl.opengl.GL11;
import org.nschmidt.ldparteditor.enums.View;

/**
 * @author nils
 *
 */
public final class PGData2 extends PGData {
    final float x1;
    final float y1;
    final float z1;
    final float x2;
    final float y2;
    final float z2;
    public PGData2(float x1, float y1, float z1, float x2, float y2, float z2) {
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }
    @Override
    public void drawBFCprimitive(int drawOnlyMode) {
        if (drawOnlyMode == 1) return;
        GL11.glLineWidth(View.lineWidthGL[0]);
        GL11.glColor4f(0f, 0f, 0f, 1f);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x2, y2, z2);
        GL11.glEnd();
    }
    @Override
    public int type() {
        return 2;
    }
    public static PGData2 clone(PGData2 o) {
        return new PGData2(o.x1, o.y1, o.z1, o.x2, o.y2, o.z2);
    }
}