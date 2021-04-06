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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Set;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.enums.Axis;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;
import org.nschmidt.ldparteditor.i18n.I18n;

class VM23FlatSubfileTester extends VM22TJunctionFixer {

    protected VM23FlatSubfileTester(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public Axis isFlatOnAxis(GData1 ref) {

        if (ref == null) return Axis.NONE;

        Matrix4f tMatrix = (Matrix4f) ref.accurateLocalMatrix.getMatrix4f().invert();

        boolean plainOnX = true;
        boolean plainOnY = true;
        boolean plainOnZ = true;

        Set<VertexInfo> verts = lineLinkedToVertices.get(ref);
        if (verts == null) return Axis.NONE;
        for (VertexInfo vi : verts) {
            Vector4f vert = vi.vertex.toVector4f();
            vert.setX(vert.x / 1000f);
            vert.setY(vert.y / 1000f);
            vert.setZ(vert.z / 1000f);
            Vector4f vert2 = Matrix4f.transform(tMatrix, vert, null);

            if (plainOnX && Math.abs(vert2.x) > 0.001f) {
                plainOnX = false;
            }
            if (plainOnY && Math.abs(vert2.y) > 0.001f) {
                plainOnY = false;
            }
            if (plainOnZ && Math.abs(vert2.z) > 0.001f) {
                plainOnZ = false;
            }
            if (!plainOnX && !plainOnY && !plainOnZ) {
                return Axis.NONE;
            }
        }

        if (plainOnX) {
            return Axis.X;
        } else if (plainOnY) {
            return Axis.Y;
        } else if (plainOnZ) {
            return Axis.Z;
        } else {
            return Axis.NONE;
        }
    }

    public ArrayList<ParsingResult> checkForFlatScaling(GData1 ref) {
        ArrayList<ParsingResult> result = new ArrayList<>();

        Matrix4f tMatrix = (Matrix4f) ref.accurateLocalMatrix.getMatrix4f().invert();

        boolean plainOnX = true;
        boolean plainOnY = true;
        boolean plainOnZ = true;

        Set<VertexInfo> verts = lineLinkedToVertices.get(ref);
        if (verts == null) return result;
        for (VertexInfo vi : verts) {
            Vector4f vert = vi.vertex.toVector4f();
            vert.setX(vert.x / 1000f);
            vert.setY(vert.y / 1000f);
            vert.setZ(vert.z / 1000f);
            Vector4f vert2 = Matrix4f.transform(tMatrix, vert, null);

            if (plainOnX && Math.abs(vert2.x) > 0.001f) {
                plainOnX = false;
            }
            if (plainOnY && Math.abs(vert2.y) > 0.001f) {
                plainOnY = false;
            }
            if (plainOnZ && Math.abs(vert2.z) > 0.001f) {
                plainOnZ = false;
            }
            if (!plainOnX && !plainOnY && !plainOnZ) {
                return result;
            }
        }

        Matrix tMatrix2 = ref.accurateLocalMatrix;
        final BigDecimal lengthX =  plainOnX ? MathHelper.sqrt(tMatrix2.M00.multiply(tMatrix2.M00).add(tMatrix2.M01.multiply(tMatrix2.M01)).add(tMatrix2.M02.multiply(tMatrix2.M02))).subtract(BigDecimal.ONE).abs() : null;
        final BigDecimal lengthY =  plainOnY ? MathHelper.sqrt(tMatrix2.M10.multiply(tMatrix2.M10).add(tMatrix2.M11.multiply(tMatrix2.M11)).add(tMatrix2.M12.multiply(tMatrix2.M12))).subtract(BigDecimal.ONE).abs() : null;
        final BigDecimal lengthZ =  plainOnZ ? MathHelper.sqrt(tMatrix2.M20.multiply(tMatrix2.M20).add(tMatrix2.M21.multiply(tMatrix2.M21)).add(tMatrix2.M22.multiply(tMatrix2.M22))).subtract(BigDecimal.ONE).abs() : null;
        // Epsilon is 0.000001 / DATHeader default value is 0.0005
        final BigDecimal epsilon = new BigDecimal("0.000001"); //$NON-NLS-1$
        if (plainOnX && epsilon.compareTo(lengthX) < 0) {
            result.add(new ParsingResult(I18n.VM_FLAT_SCALED_X, "[W02] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
        }
        if (plainOnY && epsilon.compareTo(lengthY) < 0) {
            result.add(new ParsingResult(I18n.VM_FLAT_SCALED_Y, "[W03] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
        }
        if (plainOnZ && epsilon.compareTo(lengthZ) < 0) {
            result.add(new ParsingResult(I18n.VM_FLAT_SCALED_Z, "[W04] " + I18n.DATPARSER_WARNING, ResultType.WARN)); //$NON-NLS-1$
        }

        return result;
    }
}
