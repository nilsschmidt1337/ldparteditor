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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.nschmidt.ldparteditor.enums.LDConfig;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.composite3d.IntersectorSettings;
import org.nschmidt.ldparteditor.helpers.composite3d.SymSplitterSettings;
import org.nschmidt.ldparteditor.helpers.math.Vector3d;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.text.StringHelper;

class VM13SymSplitter extends VM12IntersectorAndIsecalc {

    protected VM13SymSplitter(DatFile linkedDatFile) {
        super(linkedDatFile);
    }

    public void symSplitter(SymSplitterSettings sims) {

        if (linkedDatFile.isReadOnly()) return;

        setModifiedNoSync();
        final String originalContent = linkedDatFile.getText();

        clearSelection();

        final BigDecimal o = sims.getOffset();
        final BigDecimal p = sims.getPrecision();
        final boolean needMerge = BigDecimal.ZERO.compareTo(p) != 0;
        final int sp = sims.getSplitPlane();
        boolean wasModified = false;

        if (sims.isCutAcross()) {
            // First, do the cutting with intersector :)

            // We have to create a really big quad at the cutting plane
            final GData4 splitPlane;
            final BigDecimal a = new BigDecimal(100000000);
            final BigDecimal an = a.negate();
            switch (sp) {
            case SymSplitterSettings.Z_PLUS:
            case SymSplitterSettings.Z_MINUS:
                splitPlane = new GData4(16, .5f, .5f, .5f, 1f, a, a, o, a, an, o, an, an, o, an, a, o, new Vector3d(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE), View.DUMMY_REFERENCE, linkedDatFile);
                break;
            case SymSplitterSettings.Y_PLUS:
            case SymSplitterSettings.Y_MINUS:
                splitPlane = new GData4(16, .5f, .5f, .5f, 1f, a, o, a, a, o, an, an, o, an, an, o, a, new Vector3d(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO), View.DUMMY_REFERENCE, linkedDatFile);
                break;
            case SymSplitterSettings.X_PLUS:
            case SymSplitterSettings.X_MINUS:
                splitPlane = new GData4(16, .5f, .5f, .5f, 1f, o, a, a, o, a, an, o, an, an, o, an, a, new Vector3d(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO), View.DUMMY_REFERENCE, linkedDatFile);
                break;
            default:
                return;
            }
            linkedDatFile.addToTail(splitPlane);

            // Now we have to select the data which is cut by the split plane

            selectAll(null, true);

            {
                Set<GData> dataToCut = new HashSet<>();
                for (GData g : selectedData) {
                    if (!lineLinkedToVertices.containsKey(g)) continue;
                    final Vertex[] verts;
                    switch (g.type()) {
                    case 2:
                        verts = lines.get(g);
                        break;
                    case 3:
                        verts = triangles.get(g);
                        break;
                    case 4:
                        verts = quads.get(g);
                        break;
                    case 5:
                        Vertex[] v2 = condlines.get(g);
                        verts = new Vertex[]{v2[0], v2[1]};
                        break;
                    default:
                        continue;
                    }


                    final int targetValue = verts.length;
                    int currentValue = 0;
                    int neg = 0;
                    int pos = 0;
                    for (Vertex v : verts) {
                        switch (sp) {
                        case SymSplitterSettings.Z_PLUS:
                            if (v.zp.compareTo(o) > 0) {
                                pos++;
                            } else {
                                neg++;
                            }
                            break;
                        case SymSplitterSettings.Z_MINUS:
                            if (v.zp.compareTo(o) > 0) {
                                neg++;
                            } else {
                                pos++;
                            }
                            break;
                        case SymSplitterSettings.Y_PLUS:
                            if (v.yp.compareTo(o) > 0) {
                                pos++;
                            } else {
                                neg++;
                            }
                            break;
                        case SymSplitterSettings.Y_MINUS:
                            if (v.yp.compareTo(o) > 0) {
                                neg++;
                            } else {
                                pos++;
                            }
                            break;
                        case SymSplitterSettings.X_PLUS:
                            if (v.xp.compareTo(o) > 0) {
                                pos++;
                            } else {
                                neg++;
                            }
                            break;
                        case SymSplitterSettings.X_MINUS:
                            if (v.xp.compareTo(o) > 0) {
                                neg++;
                            } else {
                                pos++;
                            }
                            break;
                        default:
                            break;
                        }
                    }
                    currentValue = Math.max(neg, pos);
                    if (targetValue != currentValue) {
                        dataToCut.add(g);
                    }
                }

                clearSelection();

                for (GData g : dataToCut) {
                    switch (g.type()) {
                    case 2:
                        selectedLines.add((GData2) g);
                        break;
                    case 3:
                        selectedTriangles.add((GData3) g);
                        break;
                    case 4:
                        selectedQuads.add((GData4) g);
                        break;
                    case 5:
                        selectedCondlines.add((GData5) g);
                        break;
                    default:
                        continue;
                    }
                    selectedData.add(g);
                }
            }

            // Remove the quad from the selection
            selectedData.remove(splitPlane);
            selectedQuads.remove(splitPlane);

            List<OpenGLRenderer> renderers = Editor3DWindow.getRenders();
            for (OpenGLRenderer renderer : renderers) {
                if (renderer.getC3D().getLockableDatFileReference().equals(linkedDatFile)) {
                    linkedDatFile.setLastSelectedComposite(renderer.getC3D());
                }
            }

            intersector(new IntersectorSettings(), false);

            showAll();
            clearSelection();

            // Remove the split plane
            selectedData.add(splitPlane);
            selectedQuads.add(splitPlane);
            delete(false, false);
        }


        // Get header, since it is the same on all three sets (behind, between, before)
        final StringBuilder headerSb = new StringBuilder();
        final StringBuilder beforeSb = new StringBuilder();
        final StringBuilder betweenSb = new StringBuilder();
        final StringBuilder behindSb = new StringBuilder();
        final GData lastHeaderLine;
        {
            GData g = linkedDatFile.getDrawChainStart();
            while ((g = g.getNext()) != null) {
                headerSb.append(g.toString());
                headerSb.append(StringHelper.getLineDelimiter());
                if (g.getNext() == null || g.getNext().type() != 0 && !(g.getNext().type() == 6 && (
                        ((GDataBFC) g.getNext()).type == BFC.CCW_CLIP ||
                        ((GDataBFC) g.getNext()).type == BFC.CW_CLIP ||
                        ((GDataBFC) g.getNext()).type == BFC.NOCERTIFY
                        ))) {
                    break;
                }
            }
            if (g == null) {
                return;
            } else {
                lastHeaderLine = g.getBefore();
            }
        }

        // Merge vertices to the plane
        if (needMerge) {
            selectAll(null, true);
            SortedSet<Vertex> allVertices = new TreeSet<>();
            allVertices.addAll(selectedVertices);
            clearSelection();
            for (Vertex v : allVertices) {
                switch (sp) {
                case SymSplitterSettings.Z_PLUS:
                case SymSplitterSettings.Z_MINUS:
                    if (p.compareTo(v.zp.subtract(o).abs()) > 0) {
                        wasModified = changeVertexDirectFast(v, new Vertex(v.xp, v.yp, o), true) || wasModified;
                    }
                    break;
                case SymSplitterSettings.Y_PLUS:
                case SymSplitterSettings.Y_MINUS:
                    if (p.compareTo(v.yp.subtract(o).abs()) > 0) {
                        wasModified = changeVertexDirectFast(v, new Vertex(v.xp, o, v.zp), true) || wasModified;
                    }
                    break;
                case SymSplitterSettings.X_PLUS:
                case SymSplitterSettings.X_MINUS:
                    if (p.compareTo(v.xp.subtract(o).abs()) > 0) {
                        wasModified = changeVertexDirectFast(v, new Vertex(o, v.yp, v.zp), true) || wasModified;
                    }
                    break;
                default:
                    break;
                }
            }
        }

        // Separate the data according the plane and detect invalid data (identical vertices)

        selectAll(null, true);

        {

            Set<GData> subfilesWithInvertnext = new HashSet<>();

            Set<GData> before = new HashSet<>();
            Set<GData> between = new HashSet<>();
            Set<GData> behind = new HashSet<>();

            for (GData g : selectedData) {
                if (!lineLinkedToVertices.containsKey(g)) continue;
                boolean forceMiddle = false;
                final Vertex[] verts;
                switch (g.type()) {
                case 1:
                    GData1 g1 = (GData1) g;
                    {
                        boolean hasInvertnext = false;
                        GData invertNextData = g1.getBefore();
                        while (invertNextData != null && invertNextData.type() != 1 && !(invertNextData.type() == 6 && ((GDataBFC) invertNextData).type == BFC.INVERTNEXT)) {
                            invertNextData = invertNextData.getBefore();
                        }
                        if (invertNextData != null && invertNextData.type() == 6 && ((GDataBFC) invertNextData).type == BFC.INVERTNEXT) {
                            hasInvertnext = true;
                        }
                        if (hasInvertnext) {
                            subfilesWithInvertnext.add(g1);
                        }
                    }
                    String shortName = g1.shortName.trim();
                    if (shortName.contains("stug") || shortName.contains("stud.dat") || shortName.contains("stud2.dat")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        forceMiddle = true;
                    }
                    verts = new Vertex[]{new Vertex(g1.accurateProductMatrix.m30, g1.accurateProductMatrix.m31, g1.accurateProductMatrix.m32)};
                    break;
                case 2:
                    verts = lines.get(g);
                    {
                        SortedSet<Vertex> vv = new TreeSet<>();
                        vv.addAll(Arrays.asList(verts));
                        if (vv.size() != 2) continue;
                    }
                    break;
                case 3:
                    verts = triangles.get(g);
                    {
                        SortedSet<Vertex> vv = new TreeSet<>();
                        vv.addAll(Arrays.asList(verts));
                        if (vv.size() != 3) continue;
                    }
                    break;
                case 4:
                    verts = quads.get(g);
                    {
                        SortedSet<Vertex> vv = new TreeSet<>();
                        vv.addAll(Arrays.asList(verts));
                        if (vv.size() != 4) continue;
                    }
                    break;
                case 5:
                    Vertex[] v2 = condlines.get(g);
                    verts = new Vertex[]{v2[0], v2[1]};
                    {
                        SortedSet<Vertex> vv = new TreeSet<>();
                        vv.addAll(Arrays.asList(verts));
                        if (vv.size() != 2) continue;
                    }
                    break;
                default:
                    continue;
                }

                final int targetValue = verts.length;
                int currentValue = 0;
                int neg = 0;
                int pos = 0;
                for (Vertex v : verts) {
                    switch (sp) {
                    case SymSplitterSettings.Z_PLUS:
                        switch (v.zp.compareTo(o)) {
                        case -1:
                            neg++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            pos++;
                            break;
                        default:
                            break;
                        }
                        break;
                    case SymSplitterSettings.Z_MINUS:
                        switch (v.zp.compareTo(o)) {
                        case -1:
                            pos++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            neg++;
                            break;
                        default:
                            break;
                        }
                        break;
                    case SymSplitterSettings.Y_PLUS:
                        switch (v.yp.compareTo(o)) {
                        case -1:
                            neg++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            pos++;
                            break;
                        default:
                            break;
                        }
                        break;
                    case SymSplitterSettings.Y_MINUS:
                        switch (v.yp.compareTo(o)) {
                        case -1:
                            pos++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            neg++;
                            break;
                        default:
                            break;
                        }
                        break;
                    case SymSplitterSettings.X_PLUS:
                        switch (v.xp.compareTo(o)) {
                        case -1:
                            neg++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            pos++;
                            break;
                        default:
                            break;
                        }
                        break;
                    case SymSplitterSettings.X_MINUS:
                        switch (v.xp.compareTo(o)) {
                        case -1:
                            pos++;
                            break;
                        case 0:
                            neg++;
                            pos++;
                            break;
                        case 1:
                            neg++;
                            break;
                        default:
                            break;
                        }
                        break;
                    default:
                        break;
                    }
                }
                currentValue = Math.max(neg, pos);
                if (forceMiddle || targetValue == currentValue && neg == pos) {
                    between.add(g);
                } else if (targetValue != currentValue) {
                    between.add(g);
                } else if (pos == targetValue) {
                    before.add(g);
                } else if (neg == targetValue) {
                    behind.add(g);
                } else {
                    between.add(g);
                }
            }

            // Colourise only before and between

            final GData tail = linkedDatFile.getDrawChainTail();
            GColour blue = LDConfig.hasColour(1) ? LDConfig.getColour(1) : new GColour(-1, 0f, 0f, 1f, 1f);
            GColour yellow = LDConfig.hasColour(14) ? LDConfig.getColour(14) : new GColour(-1, 1f, 1f, 0f, 1f);
            GColour red = LDConfig.hasColour(4) ? LDConfig.getColour(4) : new GColour(-1, 1f, 0f, 0f, 1f);
            GColour lightBlue = LDConfig.hasColour(9) ? LDConfig.getColour(9) : new GColour(-1, 1f, .5f, .5f, 1f);
            GColour lightGreen = LDConfig.hasColour(10) ? LDConfig.getColour(10) : new GColour(-1, .5f, 1f, .5f, 1f);
            GColour violet = LDConfig.hasColour(5) ? LDConfig.getColour(5) : new GColour(-1, 1f, 0f, 1f, 1f);
            {
                GData g = lastHeaderLine;
                while ((g = g.getNext()) != null) {
                    if (g.type() < 1 || g.type() > 5) {
                        if (g.type() == 6 && ((GDataBFC) g).type == BFC.INVERTNEXT) continue;
                        beforeSb.append(g.toString());
                        if (!g.equals(tail)) beforeSb.append(StringHelper.getLineDelimiter());
                    } else {
                        if (before.contains(g)) {
                            if (sims.isColourise() && lineLinkedToVertices.containsKey(g)) {
                                switch (g.type()) {
                                case 3:
                                    beforeSb.append(((GData3) g).colourReplace(lightBlue.toString()));
                                    break;
                                case 4:
                                    beforeSb.append(((GData4) g).colourReplace(lightBlue.toString()));
                                    break;
                                case 1:
                                    if (subfilesWithInvertnext.contains(g)) {
                                        beforeSb.append(new GDataBFC(BFC.INVERTNEXT, View.DUMMY_REFERENCE).toString());
                                        beforeSb.append(StringHelper.getLineDelimiter());
                                    }
                                    beforeSb.append(((GData1) g).colourReplace(blue.toString()));
                                    break;
                                case 2:
                                    beforeSb.append(((GData2) g).colourReplace(blue.toString()));
                                    break;
                                case 5:
                                    beforeSb.append(((GData5) g).colourReplace(blue.toString()));
                                    break;
                                default:
                                    break;
                                }
                            } else {
                                if (subfilesWithInvertnext.contains(g)) {
                                    beforeSb.append(new GDataBFC(BFC.INVERTNEXT, View.DUMMY_REFERENCE).toString());
                                    beforeSb.append(StringHelper.getLineDelimiter());
                                }
                                beforeSb.append(g.toString());
                            }
                            if (!g.equals(tail)) beforeSb.append(StringHelper.getLineDelimiter());
                        }
                    }
                }
            }
            {
                GData g = lastHeaderLine;
                while ((g = g.getNext()) != null) {
                    if (g.type() < 1 || g.type() > 5) {
                        if (g.type() == 6 && ((GDataBFC) g).type == BFC.INVERTNEXT) continue;
                        betweenSb.append(g.toString());
                        if (!g.equals(tail)) betweenSb.append(StringHelper.getLineDelimiter());
                    } else {
                        if (between.contains(g)) {

                            if (sims.isValidate() && lineLinkedToVertices.containsKey(g)) {

                                boolean isSymmetrical = true;

                                if (g.type() != 1) {
                                    // Check symmetry

                                    final Vertex[] verts;
                                    switch (g.type()) {
                                    case 2:
                                        verts = lines.get(g);
                                        break;
                                    case 3:
                                        verts = triangles.get(g);
                                        break;
                                    case 4:
                                        verts = quads.get(g);
                                        break;
                                    case 5:
                                        Vertex[] v2 = condlines.get(g);
                                        verts = new Vertex[]{v2[0], v2[1]};
                                        break;
                                    default:
                                        continue;
                                    }

                                    switch (sp) {
                                    case SymSplitterSettings.Z_PLUS:
                                    case SymSplitterSettings.Z_MINUS:
                                        for (int i = 0; i < verts.length - 1; i++) {
                                            int j = (i + 1) % verts.length;
                                            BigDecimal di = verts[i].zp.subtract(o);
                                            BigDecimal dj = verts[j].zp.subtract(o);
                                            if (di.signum() != dj.signum() && di.abs().subtract(dj.abs()).abs().compareTo(p) > 0) {
                                                isSymmetrical = false;
                                                break;
                                            }
                                        }
                                        break;
                                    case SymSplitterSettings.Y_PLUS:
                                    case SymSplitterSettings.Y_MINUS:
                                        for (int i = 0; i < verts.length - 1; i++) {
                                            int j = (i + 1) % verts.length;
                                            BigDecimal di = verts[i].yp.subtract(o);
                                            BigDecimal dj = verts[j].yp.subtract(o);
                                            if (di.signum() != dj.signum() && di.abs().subtract(dj.abs()).abs().compareTo(p) > 0) {
                                                isSymmetrical = false;
                                                break;
                                            }
                                        }
                                        break;
                                    case SymSplitterSettings.X_PLUS:
                                    case SymSplitterSettings.X_MINUS:
                                        for (int i = 0; i < verts.length - 1; i++) {
                                            int j = (i + 1) % verts.length;
                                            BigDecimal di = verts[i].xp.subtract(o);
                                            BigDecimal dj = verts[j].xp.subtract(o);
                                            if (di.signum() != dj.signum() && di.abs().subtract(dj.abs()).abs().compareTo(p) > 0) {
                                                isSymmetrical = false;
                                                break;
                                            }
                                        }
                                        break;
                                    default:
                                        break;
                                    }
                                }

                                switch (g.type()) {
                                case 3:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData3) g).colourReplace(yellow.toString()));
                                    } else {
                                        betweenSb.append(((GData3) g).colourReplace(violet.toString()));
                                    }
                                    break;
                                case 4:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData4) g).colourReplace(yellow.toString()));
                                    } else {
                                        betweenSb.append(((GData4) g).colourReplace(violet.toString()));
                                    }
                                    break;
                                case 1:
                                    if (subfilesWithInvertnext.contains(g)) {
                                        betweenSb.append(new GDataBFC(BFC.INVERTNEXT, View.DUMMY_REFERENCE).toString());
                                        betweenSb.append(StringHelper.getLineDelimiter());
                                    }
                                    betweenSb.append(((GData1) g).colourReplace(yellow.toString()));
                                    break;
                                case 2:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData2) g).colourReplace(red.toString()));
                                    } else {
                                        betweenSb.append(((GData2) g).colourReplace(lightGreen.toString()));
                                    }

                                    break;
                                case 5:
                                    if (isSymmetrical) {
                                        betweenSb.append(((GData5) g).colourReplace(red.toString()));
                                    } else {
                                        betweenSb.append(((GData5) g).colourReplace(lightGreen.toString()));
                                    }
                                    break;
                                default:
                                    break;
                                }

                            } else if (sims.isColourise() && lineLinkedToVertices.containsKey(g)) {
                                switch (g.type()) {
                                case 3:
                                    betweenSb.append(((GData3) g).colourReplace(yellow.toString()));
                                    break;
                                case 4:
                                    betweenSb.append(((GData4) g).colourReplace(yellow.toString()));
                                    break;
                                case 1:
                                    if (subfilesWithInvertnext.contains(g)) {
                                        betweenSb.append(new GDataBFC(BFC.INVERTNEXT, View.DUMMY_REFERENCE).toString());
                                        betweenSb.append(StringHelper.getLineDelimiter());
                                    }
                                    betweenSb.append(((GData1) g).colourReplace(yellow.toString()));
                                    break;
                                case 2:
                                    betweenSb.append(((GData2) g).colourReplace(red.toString()));
                                    break;
                                case 5:
                                    betweenSb.append(((GData5) g).colourReplace(red.toString()));
                                    break;
                                default:
                                    break;
                                }
                            } else {
                                if (subfilesWithInvertnext.contains(g)) {
                                    betweenSb.append(new GDataBFC(BFC.INVERTNEXT, View.DUMMY_REFERENCE).toString());
                                    betweenSb.append(StringHelper.getLineDelimiter());
                                }
                                betweenSb.append(g.toString());
                            }
                            if (!g.equals(tail)) betweenSb.append(StringHelper.getLineDelimiter());
                        }
                    }
                }
            }
            {
                GData g = lastHeaderLine;
                while ((g = g.getNext()) != null) {
                    if (g.type() < 1 || g.type() > 5) {
                        if (g.type() == 6 && ((GDataBFC) g).type == BFC.INVERTNEXT) continue;
                        behindSb.append(g.toString());
                        if (!g.equals(tail)) behindSb.append(StringHelper.getLineDelimiter());
                    } else {
                        if (behind.contains(g)) {
                            if (subfilesWithInvertnext.contains(g)) {
                                behindSb.append(new GDataBFC(BFC.INVERTNEXT, View.DUMMY_REFERENCE).toString());
                                behindSb.append(StringHelper.getLineDelimiter());
                            }
                            behindSb.append(g.toString());
                            if (!g.equals(tail)) behindSb.append(StringHelper.getLineDelimiter());
                        }
                    }
                }
            }

            String headerS = headerSb.toString();
            String beforeS = beforeSb.toString();
            String betweenS = betweenSb.toString();
            String behindS = behindSb.toString();

            if (!headerS.endsWith(StringHelper.getLineDelimiter())) {
                headerS = headerS + StringHelper.getLineDelimiter();
            }
            headerS = headerS + I18n.VM_SYMSPLITTER_FRONT + StringHelper.getLineDelimiter();
            if (!beforeS.endsWith(StringHelper.getLineDelimiter())) {
                beforeS = beforeS + StringHelper.getLineDelimiter();
            }
            beforeS = beforeS + I18n.VM_SYMSPLITTER_BETWEEN + StringHelper.getLineDelimiter();
            if (!betweenS.endsWith(StringHelper.getLineDelimiter())) {
                betweenS = betweenS + StringHelper.getLineDelimiter();
            }
            betweenS = betweenS + I18n.VM_SYMSPLITTER_BEHIND + StringHelper.getLineDelimiter();

            String symSplitterOutput = headerS + beforeS + betweenS + behindS;

            if (wasModified || !symSplitterOutput.equals(originalContent)) {
                if (!Project.getUnsavedFiles().contains(linkedDatFile)) {
                    Project.addUnsavedFile(linkedDatFile);
                    Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                }

                GDataCSG.resetCSG(linkedDatFile, false);
                GDataCSG.forceRecompile(linkedDatFile);
                setModifiedNoSync();
                linkedDatFile.setText(symSplitterOutput);
                linkedDatFile.parseForData(true);

                setModifiedNoSync();

                // Separate the data according the plane and hide or show it

                if (sims.getHideLevel() > 0) {

                    selectAll(null, true);

                    before.clear();
                    between.clear();
                    behind.clear();

                    for (GData g : selectedData) {
                        if (!lineLinkedToVertices.containsKey(g)) continue;
                        boolean forceMiddle = false;
                        final Vertex[] verts;
                        switch (g.type()) {
                        case 1:
                            GData1 g1 = (GData1) g;
                            String shortName = g1.shortName.trim();
                            if (shortName.contains("stug") || shortName.contains("stud.dat") || shortName.contains("stud2.dat")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                forceMiddle = true;
                            }
                            verts = new Vertex[]{new Vertex(g1.accurateProductMatrix.m30, g1.accurateProductMatrix.m31, g1.accurateProductMatrix.m32)};
                            break;
                        case 2:
                            verts = lines.get(g);
                            {
                                SortedSet<Vertex> vv = new TreeSet<>();
                                vv.addAll(Arrays.asList(verts));
                                if (vv.size() != 2) continue;
                            }
                            break;
                        case 3:
                            verts = triangles.get(g);
                            {
                                SortedSet<Vertex> vv = new TreeSet<>();
                                vv.addAll(Arrays.asList(verts));
                                if (vv.size() != 3) continue;
                            }
                            break;
                        case 4:
                            verts = quads.get(g);
                            {
                                SortedSet<Vertex> vv = new TreeSet<>();
                                vv.addAll(Arrays.asList(verts));
                                if (vv.size() != 4) continue;
                            }
                            break;
                        case 5:
                            Vertex[] v2 = condlines.get(g);
                            verts = new Vertex[]{v2[0], v2[1]};
                            {
                                SortedSet<Vertex> vv = new TreeSet<>();
                                vv.addAll(Arrays.asList(verts));
                                if (vv.size() != 2) continue;
                            }
                            break;
                        default:
                            continue;
                        }

                        final int targetValue = verts.length;
                        int currentValue = 0;
                        int neg = 0;
                        int pos = 0;
                        for (Vertex v : verts) {
                            switch (sp) {
                            case SymSplitterSettings.Z_PLUS:
                                switch (v.zp.compareTo(o)) {
                                case -1:
                                    neg++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    pos++;
                                    break;
                                default:
                                    break;
                                }
                                break;
                            case SymSplitterSettings.Z_MINUS:
                                switch (v.zp.compareTo(o)) {
                                case -1:
                                    pos++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    neg++;
                                    break;
                                default:
                                    break;
                                }
                                break;
                            case SymSplitterSettings.Y_PLUS:
                                switch (v.yp.compareTo(o)) {
                                case -1:
                                    neg++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    pos++;
                                    break;
                                default:
                                    break;
                                }
                                break;
                            case SymSplitterSettings.Y_MINUS:
                                switch (v.yp.compareTo(o)) {
                                case -1:
                                    pos++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    neg++;
                                    break;
                                default:
                                    break;
                                }
                                break;
                            case SymSplitterSettings.X_PLUS:
                                switch (v.xp.compareTo(o)) {
                                case -1:
                                    neg++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    pos++;
                                    break;
                                default:
                                    break;
                                }
                                break;
                            case SymSplitterSettings.X_MINUS:
                                switch (v.xp.compareTo(o)) {
                                case -1:
                                    pos++;
                                    break;
                                case 0:
                                    neg++;
                                    pos++;
                                    break;
                                case 1:
                                    neg++;
                                    break;
                                default:
                                    break;
                                }
                                break;
                            default:
                                break;
                            }
                        }
                        currentValue = Math.max(neg, pos);
                        if (forceMiddle || targetValue == currentValue && neg == pos) {
                            between.add(g);
                        } else if (targetValue != currentValue) {
                            between.add(g);
                        } else if (pos == targetValue) {
                            before.add(g);
                        } else if (neg == targetValue) {
                            behind.add(g);
                        } else {
                            between.add(g);
                        }
                    }

                    hiddenVertices.addAll(selectedVertices);
                    clearSelection();

                    switch (sims.getHideLevel()) {
                    case 1: // between
                        for (GData g : before) {
                            hide(g);
                        }
                        for (GData g : behind) {
                            hide(g);
                        }
                        break;
                    case 2: // before
                        for (GData g : between) {
                            hide(g);
                        }
                        for (GData g : behind) {
                            hide(g);
                        }
                        break;
                    case 3: // behind
                        for (GData g : before) {
                            hide(g);
                        }
                        for (GData g : between) {
                            hide(g);
                        }
                        break;
                    default:
                        break;
                    }
                }

                if (isModified()) {
                    setModified(true, true);
                }

            } else {
                setModified(false, true);
            }

        }
        validateState();
    }

    private void hide(GData gdata) {
        gdata.hide();
        hiddenData.add(gdata);
        if (gdata.type() == 1) {
            hide((GData1) gdata);
        }
    }

    private void hide(GData1 gdata) {
        GData g = gdata.myGData;
        while ((g = g.next) != null) {
            hide(g);
        }
    }
}
