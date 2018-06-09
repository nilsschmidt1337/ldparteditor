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
package org.nschmidt.ldparteditor.helpers.composite3d;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.nschmidt.csg.CSG;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.ObjectMode;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Manipulator;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.vertexwindow.VertexWindow;

/**
 * Manages status text updates, which are triggered by the {@linkplain Composite3D} and
 * changing the {@linkplain Editor3DWindow} text in the status bar
 *
 * @author nils
 *
 */
public enum GuiStatusManager {
    INSTANCE;

    private static DecimalFormat df = new java.text.DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.LOCALE));
    private static DecimalFormat df2 = new java.text.DecimalFormat(View.NUMBER_FORMAT2F, new DecimalFormatSymbols(MyLanguage.LOCALE));

    /**
     * Updates the status text from {@linkplain Editor3DWindow}
     *
     * @param c3d
     *            the active {@linkplain Composite3D}
     */
    public static synchronized void updateStatus(Composite3D c3d) {
        try {
            final StringBuilder sb = new StringBuilder();
            if (c3d.isClassicPerspective()) {
                sb.append("["); //$NON-NLS-1$
                sb.append(c3d.getPerspectiveCalculator().getPerspectiveString(c3d.getPerspectiveIndex()));
                sb.append("] "); //$NON-NLS-1$
            }

            final VertexManager vm = c3d.getVertexManager();
            final Set<Vertex> vs = vm.getSelectedVertices();

            sb.append(vm.getSlantingMatrixProjectorStatusString());

            updateSelection(sb, vm);
            if (vs.size() == 1) {
                try {
                    Vertex v = vs.iterator().next();
                    sb.append(" Vertex @  ["); //$NON-NLS-1$ I18N Needs translation!
                    sb.append(df.format(v.X.multiply(View.unit_factor)));
                    sb.append("; "); //$NON-NLS-1$
                    sb.append(df.format(v.Y.multiply(View.unit_factor)));
                    sb.append("; "); //$NON-NLS-1$
                    sb.append(df.format(v.Z.multiply(View.unit_factor)));
                    sb.append("]"); //$NON-NLS-1$
                } catch (NoSuchElementException consumed) {}
            }

            sb.append(" "); //$NON-NLS-1$
            sb.append(c3d.getLockableDatFileReference().getShortName());
            sb.append(", "); //$NON-NLS-1$
            sb.append(I18n.PERSPECTIVE_Zoom);
            sb.append(": "); //$NON-NLS-1$
            sb.append(df2.format(Math.round(c3d.getZoom() * 10000000) / 100f));
            sb.append("% ["); //$NON-NLS-1$
            BigDecimal[] cursor3D = c3d.getCursorSnapped3Dprecise();
            sb.append(df.format(cursor3D[0].multiply(View.unit_factor)));
            sb.append("; "); //$NON-NLS-1$
            sb.append(df.format(cursor3D[1].multiply(View.unit_factor)));
            sb.append("; "); //$NON-NLS-1$
            sb.append(df.format(cursor3D[2].multiply(View.unit_factor)));
            sb.append("] "); //$NON-NLS-1$

            Manipulator m = c3d.getManipulator();
            if (m.isModified()) {
                Matrix t = m.getTempTransformationAccurate();
                double dax = m.getAccurateRotationX() / Math.PI * 180.0 % 360;
                double day = m.getAccurateRotationY() / Math.PI * 180.0 % 360;
                double daz = m.getAccurateRotationZ() / Math.PI * 180.0 % 360;
                if (dax == 0.0 && day == 0.0 && daz == 0.0) {
                    sb.append("delta: ["); //$NON-NLS-1$ I18N Needs translation!
                    sb.append(df.format(t.M30));
                    sb.append("; "); //$NON-NLS-1$
                    sb.append(df.format(t.M31));
                    sb.append("; "); //$NON-NLS-1$
                    sb.append(df.format(t.M32));
                } else {
                    sb.append("rotation: ["); //$NON-NLS-1$ I18N Needs translation!
                    sb.append(df.format(dax));
                    sb.append("; "); //$NON-NLS-1$
                    sb.append(df.format(day));
                    sb.append("; "); //$NON-NLS-1$
                    sb.append(df.format(daz));
                }
                sb.append("] "); //$NON-NLS-1$
            }

            final Editor3DWindow win = Editor3DWindow.getWindow();
            if (win.isMovingAdjacentData()) {
                sb.append(I18n.E3D_AdjacentWarningStatus);
            }

            final SelectorSettings sels = win.loadSelectorSettings();
            final ObjectMode om = win.getWorkingType();
            if (om == ObjectMode.FACES) {
                if (sels.isTriangles() && !sels.isQuads()) {
                    sb.append(I18n.E3D_OnlyTriangles);
                } else if (sels.isQuads() && !sels.isTriangles()) {
                    sb.append(I18n.E3D_OnlyQuads);
                }
            } else if (om == ObjectMode.LINES) {
                if (sels.isLines() && !sels.isCondlines()) {
                    sb.append(I18n.E3D_OnlyLines);
                } else if (sels.isCondlines() && !sels.isLines()) {
                    sb.append(I18n.E3D_OnlyCondlines);
                }
            }

            if (Math.abs(CSG.timeOfLastOptimization - System.currentTimeMillis()) < 10000) {
                sb.append(" CSG: "); //$NON-NLS-1$
                sb.append(df2.format(CSG.globalOptimizationRate));
                sb.append("%"); //$NON-NLS-1$
            }

            Editor3DWindow.getStatusLabel().setText(sb.toString());
            Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
            // TODO Linux only??? Editor3DWindow.getStatusLabel().update();

            VertexWindow.placeVertexWindow();

        } catch (SWTException swtex) {
            NLogger.debug(GuiStatusManager.class, "Uncritical SWTExecption. Widget is disposed."); //$NON-NLS-1$
            NLogger.debug(GuiStatusManager.class, swtex);
        }
    }

    public static synchronized void updateStatus(DatFile df) {
        final StringBuilder sb = new StringBuilder();
        final VertexManager vm = df.getVertexManager();

        sb.append(vm.getSlantingMatrixProjectorStatusString());

        updateSelection(sb, vm);

        sb.append(" "); //$NON-NLS-1$
        sb.append(df.getShortName());
        Editor3DWindow.getStatusLabel().setText(sb.toString());
        Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
        // TODO Linux only??? Editor3DWindow.getStatusLabel().update();
    }

    public static void updateStatus() {
        Editor3DWindow.getStatusLabel().setText(I18n.E3D_NoFileSelected);
        Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
        Editor3DWindow.getStatusLabel().update();
    }

    private static void updateSelection(StringBuilder sb, VertexManager vm) {
        int selectedObjectCount = 0;
        int selectedSubfileCount = vm.getSelectedSubfiles().size();
        int selectedLineCount = vm.getSelectedLines().size();
        int selectedTriangleCount = vm.getSelectedTriangles().size();
        int selectedQuadCount = vm.getSelectedQuads().size();
        int selectedCondlineCount = vm.getSelectedCondlines().size();
        int selectedVerticesCount = vm.getSelectedVertices().size();
        selectedObjectCount = selectedSubfileCount + selectedLineCount + selectedTriangleCount + selectedQuadCount + selectedCondlineCount + selectedVerticesCount;
        if (selectedObjectCount > 0) {
            boolean needsComma = false;
            sb.append("("); //$NON-NLS-1$
            needsComma = appendSelectionInfo(sb, "Vertex", "Vertices", selectedVerticesCount, needsComma); //$NON-NLS-1$ //$NON-NLS-2$ FIXME !i18n!
            needsComma = appendSelectionInfo(sb, "Line", "Lines", selectedLineCount, needsComma); //$NON-NLS-1$ //$NON-NLS-2$ FIXME !i18n!
            needsComma = appendSelectionInfo(sb, "Triangle", "Triangles", selectedTriangleCount, needsComma); //$NON-NLS-1$ //$NON-NLS-2$ FIXME !i18n!
            needsComma = appendSelectionInfo(sb, "Quad", "Quads", selectedQuadCount, needsComma); //$NON-NLS-1$ //$NON-NLS-2$ FIXME !i18n!
            needsComma = appendSelectionInfo(sb, "Condline", "Condlines", selectedCondlineCount, needsComma); //$NON-NLS-1$ //$NON-NLS-2$ FIXME !i18n!
            needsComma = appendSelectionInfo(sb, "Subfile", "Subfiles", selectedSubfileCount, needsComma); //$NON-NLS-1$ //$NON-NLS-2$ FIXME !i18n!
            sb.append(") "); //$NON-NLS-1$
        }
    }

    private static boolean appendSelectionInfo(StringBuilder sb, String singular, String plural, int count, boolean needsComma) {
        boolean doAppend = count > 0;
        if (doAppend) {
            if (needsComma) {
                sb.append(", "); //$NON-NLS-1$
            }
            sb.append(count);
            sb.append(" "); //$NON-NLS-1$
            if (count == 1) {
                sb.append(singular);
            } else {
                sb.append(plural);
            }
        }
        return doAppend;
    }
}
