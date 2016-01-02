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

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.helpers.math.MathHelper;

class VM05Distance extends VM04Rectifier {

    protected VM05Distance(DatFile linkedDatFile) {
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
            if (verts == null) continue;
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
            if (verts == null) continue;
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
            if (verts == null) continue;
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
            if (verts == null) continue;
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
            if (verts == null) continue;
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
            if (verts == null) continue;
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

    public Object[] getMinimalDistanceVerticesToLines(Vertex vertex, boolean autoCompleteSelection) {
        double minDist = Double.MAX_VALUE;
        Vector4f vp = vertex.toVector4f();
        Object[] result = new Object[4];
        Vector4f result2 = new Vector4f();
        Set<GData3> ts;
        Set<GData4> qs;
        Set<GData2> ls;
        Set<GData5> cs;

        if (selectedTriangles.isEmpty()) {
            if (selectedQuads.isEmpty()) {
                if (autoCompleteSelection) {
                    ts = triangles.keySet();
                    qs = quads.keySet();
                } else {
                    ts = new HashSet<GData3>();
                    qs = new HashSet<GData4>();
                }
            } else {
                if (autoCompleteSelection) {
                    ts = triangles.keySet();
                } else {
                    ts = new HashSet<GData3>();
                }
                qs = selectedQuads;
            }
        } else {
            if (selectedQuads.isEmpty()) {
                ts = selectedTriangles;
                if (autoCompleteSelection) {
                    qs = quads.keySet();
                } else {
                    qs = new HashSet<GData4>();
                }
            } else {
                ts = selectedTriangles;
                qs = selectedQuads;
            }
        }

        if (selectedLines.isEmpty()) {
            if (selectedCondlines.isEmpty()) {
                if (autoCompleteSelection) {
                    ls = lines.keySet();
                    cs = condlines.keySet();
                } else {
                    ls = new HashSet<GData2>();
                    cs = new HashSet<GData5>();
                }
            } else {
                if (autoCompleteSelection) {
                    ls = lines.keySet();
                } else {
                    ls = new HashSet<GData2>();
                }
                cs = selectedCondlines;
            }
        } else {
            if (selectedCondlines.isEmpty()) {
                ls = selectedLines;
                if (autoCompleteSelection) {
                    cs = condlines.keySet();
                } else {
                    cs = new HashSet<GData5>();
                }
            } else {
                ls = selectedLines;
                cs = selectedCondlines;
            }
        }

        for (GData2 g2 : ls) {
            Vertex[] verts = lines.get(g2);
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[1];
                    result2 = v1;
                    minDist = d1;
                }
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[1];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[1];
                    result[1] = verts[2];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[2];
                    result2 = v1;
                    minDist = d1;
                }
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[1];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[1];
                    result[1] = verts[2];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[2];
                    result[1] = verts[3];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[3];
                    result[1] = verts[0];
                    result2 = v1;
                    minDist = d1;
                }
            }
        }

        for (GData5 g5 : cs) {
            Vertex[] verts = condlines.get(g5);
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[1];
                    result2 = v1;
                    minDist = d1;
                }
            }
            /*
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[2];
                    result2 = v1;
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    result[0] = verts[0];
                    result[1] = verts[3];
                    result2 = v1;
                    minDist = d1;
                }
            }
            /*
             */
        }

        if (minDist == Double.MAX_VALUE) {
            result[0] = new Vertex(vertex.X, vertex.Y, vertex.Z);
            result[1] = new Vertex(vertex.X, vertex.Y, vertex.Z);
            result[2] = new Vertex(vertex.X, vertex.Y, vertex.Z);
            result[3] = minDist;
            return result;
        }
        result[0] = new Vertex(((Vertex) result[0]).X, ((Vertex) result[0]).Y, ((Vertex) result[0]).Z);
        result[1] = new Vertex(((Vertex) result[1]).X, ((Vertex) result[1]).Y, ((Vertex) result[1]).Z);
        result[2] = new Vertex(result2.x, result2.y, result2.z);
        result[3] = minDist;
        return result;
    }

    public Vertex[] getMinimalDistanceVerticesToTriangleEdges(Vertex vertex, Vertex vertexA, Vertex vertexB, Vertex vertexC, Composite3D c3d) {

        Vector4f triA = new Vector4f(vertexA.toVector4f());
        Vector4f triB = new Vector4f(vertexB.toVector4f());
        Vector4f triC = new Vector4f(vertexC.toVector4f());
        final Matrix4f vport = c3d.getViewport();
        Matrix4f.transform(vport, triA, triA);
        Matrix4f.transform(vport, triB, triB);
        Matrix4f.transform(vport, triC, triC);

        double minDist = Double.MAX_VALUE;
        Vertex[] result = new Vertex[2];
        {
            Vector4f v1 = MathHelper.getNearestPointToLineSegment(vertexA.x, vertexA.y, vertexA.z, vertexB.x, vertexB.y, vertexB.z, vertex.x, vertex.y, vertex.z);
            float d1 = Vector4f.sub(v1, vertex.toVector4f(), null).length();
            if (d1 < minDist) {
                Vector4f vert = vertex.toVector4f();
                Matrix4f.transform(vport, vert, vert);
                Vector4f ip1 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triA, triA, triC);
                if (ip1 == null) {
                    Vector4f ip2 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triA, triB, triC);
                    if (ip2 == null) {
                        Vector4f ip3 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triB, triA, triC);
                        if (ip3 == null) {
                            Vector4f ip4 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triB, triB, triC);
                            if (ip4 == null) {
                                result[0] = vertexA;
                                result[1] = vertexB;
                                minDist = d1;
                            }
                        }
                    }
                }
            }
        }
        {
            Vector4f v1 = MathHelper.getNearestPointToLineSegment(vertexB.x, vertexB.y, vertexB.z, vertexC.x, vertexC.y, vertexC.z, vertex.x, vertex.y, vertex.z);
            float d1 = Vector4f.sub(v1, vertex.toVector4f(), null).length();
            if (d1 < minDist) {
                Vector4f vert = vertex.toVector4f();
                Matrix4f.transform(vport, vert, vert);
                Vector4f ip1 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triC, triA, triB);
                if (ip1 == null) {
                    Vector4f ip2 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triC, triA, triC);
                    if (ip2 == null) {
                        Vector4f ip3 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triB, triA, triB);
                        if (ip3 == null) {
                            Vector4f ip4 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triB, triA, triC);
                            if (ip4 == null) {
                                result[0] = vertexB;
                                result[1] = vertexC;
                                minDist = d1;
                            }
                        }
                    }
                }
            }
        }
        {
            Vector4f v1 = MathHelper.getNearestPointToLineSegment(vertexA.x, vertexA.y, vertexA.z, vertexC.x, vertexC.y, vertexC.z, vertex.x, vertex.y, vertex.z);
            float d1 = Vector4f.sub(v1, vertex.toVector4f(), null).length();
            if (d1 < minDist) {
                Vector4f vert = new Vector4f(vertex.toVector4f());
                Matrix4f.transform(vport, vert, vert);
                Vector4f ip1 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triC, triA, triB);
                if (ip1 == null) {
                    Vector4f ip2 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triC, triB, triC);
                    if (ip2 == null) {
                        Vector4f ip3 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triA, triA, triB);
                        if (ip3 == null) {
                            Vector4f ip4 = MathHelper.intersectionBetweenTwoLinesStrict2D(vert, triA, triB, triC);
                            if (ip4 == null) {
                                result[0] = vertexA;
                                result[1] = vertexC;
                                minDist = d1;
                            }
                        }
                    }
                }
            }
        }
        if (minDist == Double.MAX_VALUE) {
            result[0] = new Vertex(vertex.X, vertex.Y, vertex.Z);
            result[1] = new Vertex(vertex.X, vertex.Y, vertex.Z);
            return result;
        }
        return result;
    }

    public Vertex getMinimalDistanceVertexToLineEnd(Vertex vertex) {
        double minDist = Double.MAX_VALUE;
        Vector4f vp = vertex.toVector4f();
        Vertex targetVertex = new Vertex(vp);
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
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData5 g5 : cs) {
            Vertex[] verts = condlines.get(g5);
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            /*
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLinePoints(verts[0].x, verts[0].y, verts[0].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            /*
             */
        }

        if (minDist == Double.MAX_VALUE)
            return new Vertex(vp);
        return targetVertex;
    }

    public Vertex getMinimalDistanceVertexToLines(Vertex vertex) {
        double minDist = Double.MAX_VALUE;
        Vector4f vp = vertex.toVector4f();
        Vertex targetVertex = new Vertex(vp);
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
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData3 g3 : ts) {
            Vertex[] verts = triangles.get(g3);
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
        }

        for (GData5 g5 : cs) {
            Vertex[] verts = condlines.get(g5);
            if (verts == null) continue;
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            /*
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            {
                Vector4f v1 = MathHelper.getNearestPointToLineSegment(verts[0].x, verts[0].y, verts[0].z, verts[3].x, verts[3].y, verts[3].z, vertex.x, vertex.y, vertex.z);
                float d1 = Vector4f.sub(v1, vp, null).length();
                if (d1 < minDist) {
                    targetVertex = new Vertex(v1);
                    minDist = d1;
                }
            }
            /*
             */
        }

        if (minDist == Double.MAX_VALUE)
            return new Vertex(vp);
        return targetVertex;
    }

    public Vertex getMinimalDistanceVertexToSurfaces(Vertex vertex) {
        double minDist = Double.MAX_VALUE;
        Vector4f vp = vertex.toVector4f();
        Vertex targetVertex = new Vertex(vp);
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
            if (verts == null) continue;
            Vector4f v1 = MathHelper
                    .getNearestPointToTriangle(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
            if (v1 == null)
                continue;
            float d1 = Vector4f.sub(v1, vp, null).length();
            if (d1 < minDist) {
                targetVertex = new Vertex(v1);
                minDist = d1;
            }
        }

        for (GData4 g4 : qs) {
            Vertex[] verts = quads.get(g4);
            if (verts == null) continue;
            Vector4f v1 = MathHelper
                    .getNearestPointToTriangle(verts[0].x, verts[0].y, verts[0].z, verts[1].x, verts[1].y, verts[1].z, verts[2].x, verts[2].y, verts[2].z, vertex.x, vertex.y, vertex.z);
            if (v1 == null)
                continue;
            float d1 = Vector4f.sub(v1, vp, null).length();
            if (d1 < minDist) {
                targetVertex = new Vertex(v1);
                minDist = d1;
            }
            Vector4f v2 = MathHelper
                    .getNearestPointToTriangle(verts[2].x, verts[2].y, verts[2].z, verts[3].x, verts[3].y, verts[3].z, verts[0].x, verts[0].y, verts[0].z, vertex.x, vertex.y, vertex.z);
            if (v2 == null)
                continue;
            float d2 = Vector4f.sub(v2, vp, null).length();
            if (d2 < minDist) {
                targetVertex = new Vertex(v2);
                minDist = d2;
            }
        }

        if (minDist == Double.MAX_VALUE)
            return new Vertex(vp);
        return targetVertex;
    }
}
