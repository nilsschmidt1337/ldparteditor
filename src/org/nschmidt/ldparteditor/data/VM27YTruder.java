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
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.YTruderSettings;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.text.DatParser;

public class VM27YTruder extends VM26LineIntersector {

    private final double EPSILON = 0.000001;
    private final double SMALL = 0.01;
    private double[] nullv = new double[]{0.0,0.0,0.0};

    protected VM27YTruder(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void yTruder(YTruderSettings ys) {
        if (linkedDatFile.isReadOnly()) return;

        final double distance = ys.getDistance();
        int mode = ys.getMode();
        if (distance == 0 && (mode == 1 || mode == 4)) return;

        final Set<GData2> originalSelection = new HashSet<>();
        originalSelection.addAll(selectedLines);
        if (originalSelection.isEmpty()) return;

        final Set<GData2> newLines = new HashSet<>();
        final Set<GData3> newTriangles = new HashSet<>();
        final Set<GData4> newQuads = new HashSet<>();
        final Set<GData5> newCondlines = new HashSet<>();

        final GColour col16 = View.getLDConfigColour(16);
        final GColour lineColour = DatParser.validateColour(24, .5f, .5f, .5f, 1f).clone();
        final GColour bodyColour = DatParser.validateColour(16, col16.getR(), col16.getG(), col16.getB(), 1f).clone();

        final int MAX_LINE = originalSelection.size() * 3;
        final int MAX_TRI = originalSelection.size() * 3;
        double[][][] InLine = new double[MAX_LINE][2][3];
        int[] LineUsed = new int[MAX_LINE];
        double[][][] Surf = new double[MAX_TRI][4][3];
        double[][][] CondLine = new double[MAX_TRI][4][3];
        int[] CondFlag = new int[MAX_TRI];

        int NumSurf;
        int NumCond;

        int x=0, y=1, z=2;


        double AngleLineThr = ys.getCondlineAngleThreshold();

        int end, current;
        int surfstart;

        boolean flag = false;

        if (ys.getAxis() == 0) {
            x=1; y=0; z=2;
        } else if (ys.getAxis() == 1) {
            x=0; y=1; z=2;
        } else if (ys.getAxis() == 2) {
            x=0; y=2; z=1;
        }

        int originalLineCount = 0;
        for (GData2 gData2 : originalSelection) {
            InLine[originalLineCount][0][x] = gData2.X1.doubleValue();
            InLine[originalLineCount][0][y] = gData2.Y1.doubleValue();
            InLine[originalLineCount][0][z] = gData2.Z1.doubleValue();
            InLine[originalLineCount][1][x] = gData2.X2.doubleValue();
            InLine[originalLineCount][1][y] = gData2.Y2.doubleValue();
            InLine[originalLineCount][1][z] = gData2.Z2.doubleValue();
            LineUsed[originalLineCount]=0;
            originalLineCount++;
        }

        // Extruding...

        NumSurf=0;
        NumCond=0;
        CondFlag[NumCond]=0;
        for(int i=0; i<originalLineCount; i++)
        {
            double[] p0 = new double[3], p1 = new double[3];
            double d0, d1;
            if(LineUsed[i] == 0)
            {
                LineUsed[i]=1;
                current=i;
                end=0;
                do
                {
                    flag=false;
                    for(int j=0; j<originalLineCount; j++)
                    {
                        if(LineUsed[j] == 0)
                        {
                            for(int k=0; k<2; k++)
                            {
                                if(MANHATTAN(InLine[current][end],InLine[j][k])<SMALL)
                                {
                                    current=j;
                                    end=1-k;
                                    LineUsed[current]=1;
                                    flag=true;
                                    break;
                                }
                                if(flag) break;
                            }
                        }
                        if(flag) break;
                    }
                }
                while(flag);

                end=1-end;
                surfstart=NumSurf;
                SET(Surf[NumSurf][0], InLine[current][1-end]);
                SET(Surf[NumSurf][1], InLine[current][end]);
                SET(Surf[NumSurf][2], InLine[current][end]);
                SET(Surf[NumSurf][3], InLine[current][1-end]);
                switch (mode)
                {
                case 1 :
                    Surf[NumSurf][2][1]=Surf[NumSurf][2][1] + distance;
                    Surf[NumSurf][3][1]=Surf[NumSurf][3][1] + distance;
                    break;
                case 2:
                    Surf[NumSurf][2][1]= 2*distance - Surf[NumSurf][2][1];
                    Surf[NumSurf][3][1]= 2*distance - Surf[NumSurf][3][1] ;
                    break;
                case 3:
                    Surf[NumSurf][2][1]=distance;
                    Surf[NumSurf][3][1]=distance;
                    break;
                case 4:
                    p0[0]=0; p0[1]=Surf[NumSurf][0][1]; p0[2]=0;
                    p1[0]=0; p1[1]=Surf[NumSurf][1][1]; p1[2]=0;
                    d0=DIST(p0, Surf[NumSurf][0]);
                    d1=DIST(p1, Surf[NumSurf][1]);
                    if(d0 > EPSILON)
                    {
                        Surf[NumSurf][3][0] = Surf[NumSurf][3][0] * (d0+distance)/d0;
                        Surf[NumSurf][3][2] = Surf[NumSurf][3][2] * (d0+distance)/d0;
                    }
                    if(d1 > EPSILON)
                    {
                        Surf[NumSurf][2][0] = Surf[NumSurf][2][0] * (d1+distance)/d1;
                        Surf[NumSurf][2][2] = Surf[NumSurf][2][2] * (d1+distance)/d1;
                    }
                    double a;
                    a=Tri_Angle(Surf[NumSurf][0], Surf[NumSurf][1], Surf[NumSurf][2], Surf[NumSurf][0], Surf[NumSurf][2], Surf[NumSurf][3]);
                    if(a > 0.5)
                    {
                        SET(CondLine[NumCond][0],Surf[NumSurf][0]);
                        SET(CondLine[NumCond][1],Surf[NumSurf][2]);
                        SET(CondLine[NumCond][2],Surf[NumSurf][1]);
                        SET(CondLine[NumCond][3],Surf[NumSurf][3]);
                        CondFlag[NumCond]=5;
                        NumCond++;
                    }
                    break;

                }
                NumSurf++;
                LineUsed[current] = 2;

                do
                {
                    flag=false;
                    for(int j=0; j<originalLineCount; j++)
                    {
                        if(LineUsed[j]<2)
                        {
                            for(int k=0; k<2; k++)
                            {
                                if((MANHATTAN(InLine[current][end], InLine[j][k]) <SMALL) && (LineUsed[j]<2))
                                {
                                    current=j;
                                    end=1-k;
                                    flag=true;
                                    SET(Surf[NumSurf][0], InLine[current][1-end]);
                                    SET(Surf[NumSurf][1], InLine[current][end]);
                                    SET(Surf[NumSurf][2], InLine[current][end]);
                                    SET(Surf[NumSurf][3], InLine[current][1-end]);
                                    switch (mode)
                                    {
                                    case 1 :
                                        Surf[NumSurf][2][1]=Surf[NumSurf][2][1] + distance;
                                        Surf[NumSurf][3][1]=Surf[NumSurf][3][1] + distance;
                                        break;
                                    case 2:
                                        Surf[NumSurf][2][1]= 2*distance - Surf[NumSurf][2][1];
                                        Surf[NumSurf][3][1]= 2*distance - Surf[NumSurf][3][1] ;
                                        break;
                                    case 3:
                                        Surf[NumSurf][2][1]=distance;
                                        Surf[NumSurf][3][1]=distance;
                                        break;
                                    case 4:
                                        p0[0]=0; p0[1]=Surf[NumSurf][0][1]; p0[2]=0;
                                        p1[0]=0; p1[1]=Surf[NumSurf][1][1]; p1[2]=0;
                                        d0=DIST(p0, Surf[NumSurf][0]);
                                        d1=DIST(p1, Surf[NumSurf][1]);
                                        if(d0 > EPSILON)
                                        {
                                            Surf[NumSurf][3][0] = Surf[NumSurf][3][0] * (d0+distance)/d0;
                                            Surf[NumSurf][3][2] = Surf[NumSurf][3][2] * (d0+distance)/d0;
                                        }
                                        if(d1 > EPSILON)
                                        {
                                            Surf[NumSurf][2][0] = Surf[NumSurf][2][0] * (d1+distance)/d1;
                                            Surf[NumSurf][2][2] = Surf[NumSurf][2][2] * (d1+distance)/d1;
                                        }
                                        {
                                            SET(CondLine[NumCond][0],Surf[NumSurf][0]);
                                            SET(CondLine[NumCond][1],Surf[NumSurf][2]);
                                            SET(CondLine[NumCond][2],Surf[NumSurf][1]);
                                            SET(CondLine[NumCond][3],Surf[NumSurf][3]);
                                            CondFlag[NumCond]=5;
                                            NumCond++;
                                        }
                                        break;
                                    }
                                    SET(CondLine[NumCond][0],Surf[NumSurf][0]);
                                    SET(CondLine[NumCond][1],Surf[NumSurf][3]);
                                    SET(CondLine[NumCond][2],Surf[NumSurf][1]);
                                    SET(CondLine[NumCond][3],Surf[NumSurf-1][0]);
                                    CondFlag[NumCond]=5;
                                    NumSurf++;
                                    NumCond++;
                                    LineUsed[current] = 2;
                                }
                                if(flag) break;
                            }
                        }
                        if(flag) break;
                    }
                }
                while(flag);
                if(MANHATTAN(Surf[NumSurf-1][1], Surf[surfstart][0])<SMALL)
                {
                    SET(CondLine[NumCond][0],Surf[NumSurf-1][1]);
                    SET(CondLine[NumCond][1],Surf[NumSurf-1][2]);
                    SET(CondLine[NumCond][2],Surf[NumSurf-1][0]);
                    SET(CondLine[NumCond][3],Surf[surfstart][1]);
                    CondFlag[NumCond]=5;
                    NumCond++;
                }
                else
                {
                    SET(CondLine[NumCond][0],Surf[NumSurf-1][1]);
                    SET(CondLine[NumCond][1],Surf[NumSurf-1][2]);
                    CondFlag[NumCond]=2;
                    NumCond++;
                    SET(CondLine[NumCond][0],Surf[surfstart][0]);
                    SET(CondLine[NumCond][1],Surf[surfstart][3]);
                    CondFlag[NumCond]=2;
                    NumCond++;
                }
            }
        }

        for(int k = 0; k<NumSurf; k++)
        {
            if((MANHATTAN(Surf[k][0], Surf[k][3])<SMALL) && MANHATTAN(Surf[k][1], Surf[k][2])<SMALL) continue;
            if(MANHATTAN(Surf[k][0], Surf[k][3])<SMALL)
            {
                Vertex v1 = new Vertex(new BigDecimal(Surf[k][0][x]), new BigDecimal(Surf[k][0][y]), new BigDecimal(Surf[k][0][z]));
                Vertex v2 = new Vertex(new BigDecimal(Surf[k][1][x]), new BigDecimal(Surf[k][1][y]), new BigDecimal(Surf[k][1][z]));
                Vertex v3 = new Vertex(new BigDecimal(Surf[k][2][x]), new BigDecimal(Surf[k][2][y]), new BigDecimal(Surf[k][2][z]));
                newTriangles.add(new GData3(
                        bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                        v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
            }
            else if(MANHATTAN(Surf[k][1], Surf[k][2])<SMALL)
            {
                Vertex v1 = new Vertex(new BigDecimal(Surf[k][0][x]), new BigDecimal(Surf[k][0][y]), new BigDecimal(Surf[k][0][z]));
                Vertex v2 = new Vertex(new BigDecimal(Surf[k][1][x]), new BigDecimal(Surf[k][1][y]), new BigDecimal(Surf[k][1][z]));
                Vertex v3 = new Vertex(new BigDecimal(Surf[k][3][x]), new BigDecimal(Surf[k][3][y]), new BigDecimal(Surf[k][3][z]));
                newTriangles.add(new GData3(
                        bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                        v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
            }
            else if(Tri_Angle(Surf[k][0], Surf[k][1], Surf[k][2], Surf[k][0], Surf[k][2], Surf[k][3]) <= 0.5)
            {
                Vertex v1 = new Vertex(new BigDecimal(Surf[k][0][x]), new BigDecimal(Surf[k][0][y]), new BigDecimal(Surf[k][0][z]));
                Vertex v2 = new Vertex(new BigDecimal(Surf[k][1][x]), new BigDecimal(Surf[k][1][y]), new BigDecimal(Surf[k][1][z]));
                Vertex v3 = new Vertex(new BigDecimal(Surf[k][2][x]), new BigDecimal(Surf[k][2][y]), new BigDecimal(Surf[k][2][z]));
                Vertex v4 = new Vertex(new BigDecimal(Surf[k][3][x]), new BigDecimal(Surf[k][3][y]), new BigDecimal(Surf[k][3][z]));
                newQuads.add(new GData4(
                        bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                        v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
            }
            else
            {
                {
                    Vertex v1 = new Vertex(new BigDecimal(Surf[k][0][x]), new BigDecimal(Surf[k][0][y]), new BigDecimal(Surf[k][0][z]));
                    Vertex v2 = new Vertex(new BigDecimal(Surf[k][1][x]), new BigDecimal(Surf[k][1][y]), new BigDecimal(Surf[k][1][z]));
                    Vertex v3 = new Vertex(new BigDecimal(Surf[k][2][x]), new BigDecimal(Surf[k][2][y]), new BigDecimal(Surf[k][2][z]));
                    newTriangles.add(new GData3(
                            bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                            v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
                }
                {
                    Vertex v1 = new Vertex(new BigDecimal(Surf[k][0][x]), new BigDecimal(Surf[k][0][y]), new BigDecimal(Surf[k][0][z]));
                    Vertex v2 = new Vertex(new BigDecimal(Surf[k][2][x]), new BigDecimal(Surf[k][2][y]), new BigDecimal(Surf[k][2][z]));
                    Vertex v3 = new Vertex(new BigDecimal(Surf[k][3][x]), new BigDecimal(Surf[k][3][y]), new BigDecimal(Surf[k][3][z]));
                    newTriangles.add(new GData3(
                            bodyColour.getColourNumber(), bodyColour.getR(), bodyColour.getG(), bodyColour.getB(), bodyColour.getA(),
                            v1, v2, v3, View.DUMMY_REFERENCE, linkedDatFile, true));
                }
            }
        }

        for(int k=0; k<NumCond; k++)
        {
            if(MANHATTAN(CondLine[k][0], CondLine[k][1]) <SMALL) continue;
            if(CondFlag[k] == 5)
            {
                double a;
                a = Tri_Angle (CondLine[k][0], CondLine[k][1], CondLine[k][2], CondLine[k][0], CondLine[k][3], CondLine[k][1]);
                if(a<AngleLineThr)
                {
                    Vertex v1 = new Vertex(new BigDecimal(CondLine[k][0][x]), new BigDecimal(CondLine[k][0][y]), new BigDecimal(CondLine[k][0][z]));
                    Vertex v2 = new Vertex(new BigDecimal(CondLine[k][1][x]), new BigDecimal(CondLine[k][1][y]), new BigDecimal(CondLine[k][1][z]));
                    Vertex v3 = new Vertex(new BigDecimal(CondLine[k][2][x]), new BigDecimal(CondLine[k][2][y]), new BigDecimal(CondLine[k][2][z]));
                    Vertex v4 = new Vertex(new BigDecimal(CondLine[k][3][x]), new BigDecimal(CondLine[k][3][y]), new BigDecimal(CondLine[k][3][z]));
                    newCondlines.add(new GData5(
                            lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(),
                            v1, v2, v3, v4, View.DUMMY_REFERENCE, linkedDatFile));
                }
                else
                {
                    Vertex v1 = new Vertex(new BigDecimal(CondLine[k][0][x]), new BigDecimal(CondLine[k][0][y]), new BigDecimal(CondLine[k][0][z]));
                    Vertex v2 = new Vertex(new BigDecimal(CondLine[k][1][x]), new BigDecimal(CondLine[k][1][y]), new BigDecimal(CondLine[k][1][z]));
                    newLines.add(new GData2(
                            lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(),
                            v1, v2, View.DUMMY_REFERENCE, linkedDatFile, true));
                }
            }

            if(CondFlag[k] == 2)
            {
                Vertex v1 = new Vertex(new BigDecimal(CondLine[k][0][x]), new BigDecimal(CondLine[k][0][y]), new BigDecimal(CondLine[k][0][z]));
                Vertex v2 = new Vertex(new BigDecimal(CondLine[k][1][x]), new BigDecimal(CondLine[k][1][y]), new BigDecimal(CondLine[k][1][z]));
                newLines.add(new GData2(
                        lineColour.getColourNumber(), lineColour.getR(), lineColour.getG(), lineColour.getB(), lineColour.getA(),
                        v1, v2, View.DUMMY_REFERENCE, linkedDatFile, true));
            }

        }

        NLogger.debug(getClass(), "Check for identical vertices and collinearity."); //$NON-NLS-1$
        final Set<GData2> linesToDelete2 = new HashSet<GData2>();
        final Set<GData3> trisToDelete2 = new HashSet<GData3>();
        final Set<GData4> quadsToDelete2 = new HashSet<GData4>();
        final Set<GData5> condlinesToDelete2 = new HashSet<GData5>();
        {
            for (GData2 g2 : newLines) {
                Vertex[] verts = lines.get(g2);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 2) {
                    linesToDelete2.add(g2);
                }
            }
            for (GData3 g3 : newTriangles) {
                Vertex[] verts = triangles.get(g3);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 3 || g3.isCollinear()) {
                    trisToDelete2.add(g3);
                }
            }
            for (GData4 g4 : newQuads) {
                Vertex[] verts = quads.get(g4);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4 || g4.isCollinear()) {
                    quadsToDelete2.add(g4);
                }
            }
            for (GData5 g5 : newCondlines) {
                Vertex[] verts = condlines.get(g5);
                Set<Vertex> verts2 = new TreeSet<Vertex>();
                for (Vertex vert : verts) {
                    verts2.add(vert);
                }
                if (verts2.size() < 4) {
                    condlinesToDelete2.add(g5);
                }
            }
        }

        // Append the new data
        for (GData2 line : newLines) {
            linkedDatFile.addToTailOrInsertAfterCursor(line);
        }
        for (GData3 tri : newTriangles) {
            linkedDatFile.addToTailOrInsertAfterCursor(tri);
        }
        for (GData4 quad : newQuads) {
            linkedDatFile.addToTailOrInsertAfterCursor(quad);
        }
        for (GData5 condline : newCondlines) {
            linkedDatFile.addToTailOrInsertAfterCursor(condline);
        }

        NLogger.debug(getClass(), "Delete new, but invalid objects."); //$NON-NLS-1$

        clearSelection2();
        newLines.removeAll(linesToDelete2);
        newTriangles.removeAll(trisToDelete2);
        newQuads.removeAll(quadsToDelete2);
        newCondlines.removeAll(condlinesToDelete2);
        selectedLines.addAll(linesToDelete2);
        selectedTriangles.addAll(trisToDelete2);
        selectedQuads.addAll(quadsToDelete2);
        selectedCondlines.addAll(condlinesToDelete2);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);
        delete(false, false);

        // Round to 6 decimal places

        selectedLines.addAll(newLines);
        selectedTriangles.addAll(newTriangles);
        selectedQuads.addAll(newQuads);
        selectedCondlines.addAll(newCondlines);
        selectedData.addAll(selectedLines);
        selectedData.addAll(selectedTriangles);
        selectedData.addAll(selectedQuads);
        selectedData.addAll(selectedCondlines);

        NLogger.debug(getClass(), "Round."); //$NON-NLS-1$
        roundSelection(6, 10, true, false, true, true, true);

        setModified(true, true);
        validateState();

        NLogger.debug(getClass(), "Done."); //$NON-NLS-1$
    }

    private void CROSS(double[] dest, double[] left, double[] right) {
        dest[0]=left[1]*right[2]-left[2]*right[1];
        dest[1]=left[2]*right[0]-left[0]*right[2];
        dest[2]=left[0]*right[1]-left[1]*right[0];
    }

    private double DOT(double[] v1, double[] v2) {
        return v1[0]*v2[0]+v1[1]*v2[1]+v1[2]*v2[2];
    }

    private void SUB(double[] dest, double[] left, double[] right) {
        dest[0]=left[0]-right[0]; dest[1]=left[1]-right[1]; dest[2]=left[2]-right[2];
    }

    private void MULT(double[] dest, double[] v, double factor) {
        dest[0]=factor*v[0]; dest[1]=factor*v[1]; dest[2]=factor*v[2];
    }

    private void SET(double[] dest, double[] src) {
        dest[0]=src[0]; dest[1]=src[1]; dest[2]=src[2];
    }

    private double MANHATTAN(double[] v1, double[] v2) {
        return Math.abs(v1[0]-v2[0]) + Math.abs(v1[1]-v2[1]) + Math.abs(v1[2]-v2[2]);
    }

    private double DIST(double[] v1, double[] v2) {
        return Math.sqrt((v1[0]-v2[0])*(v1[0]-v2[0]) + (v1[1]-v2[1])*(v1[1]-v2[1]) + (v1[2]-v2[2])*(v1[2]-v2[2]));
    }

     // Tri_Angle computes the cosine of the angle between the planes of two triangles.
     // They are assumed to be non-degenerated
     private double Tri_Angle(double[] U0, double[] U1, double[] U2, double[] V0, double[] V1, double[] V2)
     {
         double[] Unorm = new double[3], Vnorm = new double[3];
         double[] Temp = new double[3];
         double[] U10 = new double[3], U20 = new double[3];
         double[] V10 = new double[3], V20 = new double[3];
         double len;
         SUB(U10, U1, U0);
         SUB(U20, U2, U0);
         SUB(V10, V1, V0);
         SUB(V20, V2, V0);
         CROSS(Temp, U10, U20);
         len = DIST(Temp, nullv);
         MULT(Unorm, Temp, 1/len);
         CROSS(Temp, V10, V20);
         len = DIST(Temp, nullv);
         MULT(Vnorm, Temp, 1/len);
         return (180 / 3.14159 * Math.acos(DOT(Unorm, Vnorm)));
     }
}
