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

import java.util.HashSet;
import java.util.Set;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;

public class VM05Distance extends VM04Rectifier {

    public VM05Distance(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public Vector4f getMinimalDistanceEdgeNormal(Vertex vertex) {
        Vector4f result = new Vector4f(0f, 0f, 0f, 0f);

        double minDist = Double.MAX_VALUE;
        Vector4f vp = vertex.toVector4f();
        Vertex p1 = new Vertex(vp);
        Vertex p2 = new Vertex(vp);
        Set<GData3> ts;
        Set<GData4> qs;

        Set<GData2> ls;
        Set<GData5> cs;
        if (selectedTriangles.isEmpty()) {
            if (selectedQuads.isEmpty()) {
                ts = triangles.keySet();
                qs = quads.keySet();
            } else {
                ts = triangles.keySet();
                qs = selectedQuads;
            }
        } else {
            if (selectedQuads.isEmpty()) {
                ts = selectedTriangles;
                qs = quads.keySet();
            } else {
                ts = selectedTriangles;
                qs = selectedQuads;
            }
        }

        if (selectedLines.isEmpty()) {
            if (selectedCondlines.isEmpty()) {
                ls = lines.keySet();
                cs = condlines.keySet();
            } else {
                ls = lines.keySet();
                cs = selectedCondlines;
            }
        } else {
            if (selectedCondlines.isEmpty()) {
                ls = selectedLines;
                cs = condlines.keySet();
            } else {
                ls = selectedLines;
                cs = selectedCondlines;
            }
        }

        for (GData2 g2 : ls) {
            Vertex[] verts = lines.get(g2);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[0];
                    p2 = verts[1];
                    minDist = d1;
                }
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[0];
                    p2 = verts[1];
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[1];
                    p2 = verts[2];
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[0];
                    p2 = verts[2];
                    minDist = d1;
                }
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[0];
                    p2 = verts[1];
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[1];
                    p2 = verts[2];
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[2];
                    p2 = verts[3];
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[3];
                    p2 = verts[0];
                    minDist = d1;
                }
            }
        }

        for (GData5 g5 : cs) {
            Vertex[] verts = condlines.get(g5);
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    p1 = verts[0];
                    p2 = verts[1];
                    minDist = d1;
                }
            }
        }

        HashSet<GData> commonLinkedGData = new HashSet<GData>();
        {
            Set<VertexManifestation> linked = vertexLinkedToPositionInFile.get(p1);
            Set<VertexManifestation> linked2 = vertexLinkedToPositionInFile.get(p2);
            HashSet<GData> l2 = new HashSet<GData>(linked2.size());
            for (VertexManifestation vm : linked) {
                commonLinkedGData.add(vm.getGdata());
            }
            for (VertexManifestation vm : linked2) {
                l2.add(vm.getGdata());
            }
            commonLinkedGData.retainAll(l2);
        }

        for (GData g : commonLinkedGData) {
            Vector3f n;
            switch (g.type()) {
            case 3:
                GData3 g3 = (GData3) g;
                n = new Vector3f(g3.xn, g3.yn, g3.zn);
                if (n.lengthSquared() != 0) {
                    n.normalise();
                    result.set(n.x + result.x, n.y + result.y, n.z + result.z);
                }
                break;
            case 4:
                GData4 g4 = (GData4) g;
                n = new Vector3f(g4.xn, g4.yn, g4.zn);
                if (n.lengthSquared() != 0) {
                    n.normalise();
                    result.set(n.x + result.x, n.y + result.y, n.z + result.z);
                }
                break;
            }
        }
        if (result.lengthSquared() == 0)
            return new Vector4f(0f, 0f, 1f, 1f);
        result.normalise();
        result.setW(1f);
        return result;
    }

    public Vector4f getMinimalDistanceSurfaceNormal(Vertex vertex) {
        double minDist = Double.MAX_VALUE;
        Vector4f result = new Vector4f(0f, 0f, 0f, 0f);
        Vector4f vp = vertex.toVector4f();
        Set<GData3> ts;
        Set<GData4> qs;
        if (selectedTriangles.isEmpty()) {
            if (selectedQuads.isEmpty()) {
                ts = triangles.keySet();
                qs = quads.keySet();
            } else {
                ts = triangles.keySet();
                qs = selectedQuads;
            }
        } else {
            if (selectedQuads.isEmpty()) {
                ts = selectedTriangles;
                qs = quads.keySet();
            } else {
                ts = selectedTriangles;
                qs = selectedQuads;
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            Vector4f v1 = MathHelper
                    .getNearestPointToTriangle(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
            if (v1 == null)
                continue;
            float d1 = Vector4f.sub(v1, vp, null).length();
            if (d1 < minDist) {
                result.set(g3.xn, g3.yn, g3.zn);
                minDist = d1;
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            Vector4f v1 = MathHelper
                    .getNearestPointToTriangle(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
            if (v1 == null)
                continue;
            float d1 = Vector4f.sub(v1, vp, null).length();
            if (d1 < minDist) {
                result.set(g4.xn, g4.yn, g4.zn);
                minDist = d1;
            }
            Vector4f v2 = MathHelper
                    .getNearestPointToTriangle(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
            if (v2 == null)
                continue;
            float d2 = Vector4f.sub(v2, vp, null).length();
            if (d2 < minDist) {
                result.set(g4.xn, g4.yn, g4.zn);
                minDist = d2;
            }
        }

        if (result.lengthSquared() == 0)
            return new Vector4f(0f, 0f, 1f, 1f);

        result.normalise();
        result.setW(1f);
        return result;
    }


}
