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

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.Set;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;

/**
 * @author nils
 *
 */
public final class GData1P extends GDataP {

    final FloatBuffer matrix;
    final Matrix4f productMatrix;
    final Matrix4f localMatrix;

    final String name;
    final String shortName;

    final boolean negativeDeterminant;

    boolean recursive = false;
    boolean movedTo = false;

    final GDataP myGData = new GDataInitP();

    final int depth;

    /**
     * SLOWER, FOR PRIMITIVE CHOOSER ONLY, uses no cache, uses no bounding box!
     *
     * @param tMatrix
     * @param lines
     * @param name
     * @param shortName
     * @param depth
     * @param det
     * @param pMatrix
     * @param firstRef
     */
    public GData1P(Matrix4f tMatrix, LinkedList<String> lines, String name, String shortName, int depth, boolean det, Matrix4f pMatrix,
            Set<String> alreadyParsed) {

        depth++;
        if (depth < 16) {
            this.depth = depth;
            negativeDeterminant = det;
            this.name = name;
            this.shortName = shortName;
            this.productMatrix = new Matrix4f(pMatrix);
            this.localMatrix = new Matrix4f(tMatrix);
            matrix = BufferUtils.createFloatBuffer(16);
            tMatrix.store(matrix);

            matrix.position(0);

            GDataP anchorData = myGData;

            for (String line : lines) {
                if (isNotBlank(line)) {
                    GDataP gdata = CompositePrimitive.parseLine(line, depth, pMatrix, alreadyParsed);
                    if (gdata != null) {
                        anchorData.setNext(gdata);
                        anchorData = gdata;
                    }
                }
            }
        } else {
            this.depth = depth;
            this.name = null;
            this.shortName = null;
            this.matrix = null;
            this.productMatrix = null;
            this.localMatrix = null;
            this.negativeDeterminant = false;
        }
    }

    public Matrix4f getProductMatrix() {
        return productMatrix;
    }

    private boolean isNotBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return false;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(str.charAt(i)) == false) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void drawBFCprimitive() {
        if (matrix != null) {
            byte tempWinding = GDataP.localWinding;
            boolean tempInvertNext = GDataP.globalInvertNext;
            boolean tempInvertNextFound = GDataP.globalInvertNextFound;
            boolean tempNegativeDeterminant = GDataP.globalNegativeDeterminant;
            GDataP.globalInvertNextFound = false;
            GDataP.localWinding = BFC.NOCERTIFY;
            GDataP.globalNegativeDeterminant = GDataP.globalNegativeDeterminant ^ negativeDeterminant;
            GL11.glPushMatrix();
            GL11.glMultMatrix(matrix);
            GDataP data2draw = myGData;
            if (GDataP.accumClip > 0) {
                GDataP.accumClip++;
                while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                    data2draw.drawBFCprimitive();
                GDataP.accumClip--;
            } else {
                while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                    data2draw.drawBFCprimitive();
                }
                if (GDataP.accumClip > 0)
                    GDataP.accumClip = 0;
            }
            GL11.glPopMatrix();
            GDataP.localWinding = tempWinding;
            if (tempInvertNextFound)
                GDataP.globalInvertNext = !tempInvertNext;
            GDataP.globalNegativeDeterminant = tempNegativeDeterminant;
        }
    }

    @Override
    public int type() {
        return 1;
    }

    public boolean isRecursive() {
        return recursive;
    }

    public void setRecursive(boolean recursive) {
        this.recursive = recursive;
    }

}
