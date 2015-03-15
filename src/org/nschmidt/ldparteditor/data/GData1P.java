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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.primitive.CompositePrimitive;
import org.nschmidt.ldparteditor.helpers.composite3d.ViewIdleManager;

/**
 * @author nils
 *
 */
public final class GData1P extends GDataP {

    private static Set<String> filesWithLogo1 = new HashSet<String>();
    private static Set<String> filesWithLogo2 = new HashSet<String>();

    static {
        filesWithLogo1.add("STUD.DAT"); //$NON-NLS-1$
        filesWithLogo1.add("STUD.dat"); //$NON-NLS-1$
        filesWithLogo1.add("stud.dat"); //$NON-NLS-1$
        filesWithLogo2.add("STUD2.DAT"); //$NON-NLS-1$
        filesWithLogo2.add("STUD2.dat"); //$NON-NLS-1$
        filesWithLogo2.add("stud2.dat"); //$NON-NLS-1$
    }

    final int colourNumber;

    final float r;
    final float g;
    final float b;
    final float a;

    final FloatBuffer matrix;
    final Matrix4f productMatrix;
    final Matrix4f localMatrix;
    final Matrix accurateProductMatrix;
    final Matrix accurateLocalMatrix;

    final String name;
    final String shortName;

    final Vector4f boundingBoxMin = new Vector4f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, 1f);
    final Vector4f boundingBoxMax = new Vector4f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE, 1f);

    final boolean negativeDeterminant;

    boolean recursive = false;
    boolean movedTo = false;

    final GDataP myGData = new GDataInitP();

    final int depth;

    /**
     * SLOWER, FOR PRIMITIVE CHOOSER ONLY, uses no cache, uses no bounding box!
     *
     * @param colourNumber
     * @param r
     * @param g
     * @param b
     * @param a
     * @param tMatrix
     * @param lines
     * @param name
     * @param shortName
     * @param depth
     * @param det
     * @param pMatrix
     * @param firstRef
     */
    public GData1P(int colourNumber, float r, float g, float b, float a, Matrix4f tMatrix, LinkedList<String> lines, String name, String shortName, int depth, boolean det, Matrix4f pMatrix,
            Set<String> alreadyParsed, GData1 firstRef) {

        this.accurateLocalMatrix = null;
        this.accurateProductMatrix = null;
        depth++;
        if (depth < 16) {
            this.depth = depth;
            this.boundingBoxMin.x = -10000000f;
            this.boundingBoxMin.y = -10000000f;
            this.boundingBoxMin.z = -1000000f;
            this.boundingBoxMax.x = 10000000f;
            this.boundingBoxMax.y = 10000000f;
            this.boundingBoxMax.z = 10000000f;
            negativeDeterminant = det;
            this.colourNumber = colourNumber;
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
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
                    GDataP gdata = CompositePrimitive.parseLine(line, depth, r, g, b, a, this, pMatrix, alreadyParsed);
                    if (gdata != null) {
                        anchorData.setNext(gdata);
                        anchorData = gdata;
                    }
                }
            }
        } else {
            this.depth = depth;
            this.colourNumber = 0;
            this.r = 0;
            this.g = 0;
            this.b = 0;
            this.a = 0;
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

    public Matrix getAccurateProductMatrix() {
        return accurateProductMatrix;
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
            byte tempWinding = GData.localWinding;
            boolean tempInvertNext = GData.globalInvertNext;
            boolean tempInvertNextFound = GData.globalInvertNextFound;
            boolean tempNegativeDeterminant = GData.globalNegativeDeterminant;
            GData.globalInvertNextFound = false;
            GData.localWinding = BFC.NOCERTIFY;
            GData.globalNegativeDeterminant = GData.globalNegativeDeterminant ^ negativeDeterminant;
            GL11.glPushMatrix();
            GL11.glMultMatrix(matrix);
            GDataP data2draw = myGData;
            if (GData.accumClip > 0) {
                GData.accumClip++;
                while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get())
                    data2draw.drawBFCprimitive();
                GData.accumClip--;
            } else {
                while ((data2draw = data2draw.getNext()) != null && !ViewIdleManager.pause[0].get()) {
                    data2draw.drawBFCprimitive();
                }
                if (GData.accumClip > 0)
                    GData.accumClip = 0;
            }
            GL11.glPopMatrix();
            GData.localWinding = tempWinding;
            if (tempInvertNextFound)
                GData.globalInvertNext = !tempInvertNext;
            GData.globalNegativeDeterminant = tempNegativeDeterminant;
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
