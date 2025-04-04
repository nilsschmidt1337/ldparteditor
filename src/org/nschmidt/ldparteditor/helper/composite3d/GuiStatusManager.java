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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.csg.CSG;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GData2;
import org.nschmidt.ldparteditor.data.GData5;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexInfo;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.dialog.calibrate.CalibrateDialog;
import org.nschmidt.ldparteditor.enumtype.MyLanguage;
import org.nschmidt.ldparteditor.enumtype.ObjectMode;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.AddToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToggleToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.WorkingTypeToolItem;
import org.nschmidt.ldparteditor.vertexwindow.VertexWindow;

/**
 * Manages status text updates, which are triggered by the {@linkplain Composite3D} and
 * changing the {@linkplain Editor3DWindow} text in the status bar
 */
public enum GuiStatusManager {
    INSTANCE;

    private static final DecimalFormat DF0F = new java.text.DecimalFormat(View.NUMBER_FORMAT0F, new DecimalFormatSymbols(MyLanguage.getLocale()));
    private static final DecimalFormat DF1F = new java.text.DecimalFormat(View.NUMBER_FORMAT1F, new DecimalFormatSymbols(MyLanguage.getLocale()));
    private static final DecimalFormat DF2F = new java.text.DecimalFormat(View.NUMBER_FORMAT2F, new DecimalFormatSymbols(MyLanguage.getLocale()));
    private static final DecimalFormat DF4F = new java.text.DecimalFormat(View.NUMBER_FORMAT4F, new DecimalFormatSymbols(MyLanguage.getLocale()));

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
            final int selectedVertexCount = vs.size();

            sb.append(vm.getSlantingMatrixProjectorStatusString());

            updateSelection(sb, vm);
            if (selectedVertexCount < 5 && selectedVertexCount > 0) {
                final Iterator<ScreenVertex> orderedScreenVertexIterator = getVerticesOrderedByScreenCoordinate(vs.iterator(), c3d.getPerspectiveCalculator());
                switch (selectedVertexCount) {
                case 1:
                    formatForOneSelectedVertex(sb, orderedScreenVertexIterator);
                    break;
                case 2:
                    sb.append(I18n.C3D_VERTEX_2 + " @ ~"); //$NON-NLS-1$
                    formatForTwoSelectedVertices(sb, orderedScreenVertexIterator);
                    formatForTwoSelectedVertices(sb, orderedScreenVertexIterator);
                    break;
                case 3:
                    sb.append(I18n.C3D_VERTEX_3 + " @ ~"); //$NON-NLS-1$
                    formatForThreeSelectedVertices(sb, orderedScreenVertexIterator);
                    formatForThreeSelectedVertices(sb, orderedScreenVertexIterator);
                    formatForThreeSelectedVertices(sb, orderedScreenVertexIterator);
                    break;
                case 4:
                    sb.append(I18n.C3D_VERTEX_4 + " @ ~"); //$NON-NLS-1$
                    formatForFourSelectedVertices(sb, orderedScreenVertexIterator);
                    formatForFourSelectedVertices(sb, orderedScreenVertexIterator);
                    formatForFourSelectedVertices(sb, orderedScreenVertexIterator);
                    formatForFourSelectedVertices(sb, orderedScreenVertexIterator);
                    break;
                default:
                    break;
                }
            }

            final DatFile df = c3d.getLockableDatFileReference();
            sb.append(" "); //$NON-NLS-1$
            sb.append(df.getShortNameMixedCase());
            sb.append(", "); //$NON-NLS-1$
            sb.append(I18n.PERSPECTIVE_ZOOM);
            sb.append(": "); //$NON-NLS-1$
            sb.append(DF2F.format(Math.round(c3d.getZoom() * 10000000) / 100f));
            sb.append("% ["); //$NON-NLS-1$
            BigDecimal[] cursor3D = c3d.getCursorSnapped3Dprecise();
            sb.append(DF4F.format(cursor3D[0].multiply(View.unitFactor)));
            sb.append("; "); //$NON-NLS-1$
            sb.append(DF4F.format(cursor3D[1].multiply(View.unitFactor)));
            sb.append("; "); //$NON-NLS-1$
            sb.append(DF4F.format(cursor3D[2].multiply(View.unitFactor)));
            sb.append("] "); //$NON-NLS-1$

            Manipulator m = c3d.getManipulator();
            if (m.isModified()) {
                Matrix t = m.getTempTransformationAccurate();
                double dax = m.getAccurateRotationX() / Math.PI * 180.0 % 360;
                double day = m.getAccurateRotationY() / Math.PI * 180.0 % 360;
                double daz = m.getAccurateRotationZ() / Math.PI * 180.0 % 360;
                if (dax == 0.0 && day == 0.0 && daz == 0.0) {
                    sb.append(I18n.C3D_DELTA + " ["); //$NON-NLS-1$
                    sb.append(DF4F.format(t.m30));
                    sb.append("; "); //$NON-NLS-1$
                    sb.append(DF4F.format(t.m31));
                    sb.append("; "); //$NON-NLS-1$
                    sb.append(DF4F.format(t.m32));
                } else {
                    sb.append(I18n.C3D_ROTATION + " ["); //$NON-NLS-1$
                    sb.append(DF4F.format(dax));
                    sb.append("; "); //$NON-NLS-1$
                    sb.append(DF4F.format(day));
                    sb.append("; "); //$NON-NLS-1$
                    sb.append(DF4F.format(daz));
                }
                sb.append("] "); //$NON-NLS-1$
            }

            if (MiscToggleToolItem.isMovingAdjacentData()) {
                sb.append(I18n.E3D_ADJACENT_WARNING_STATUS);
            }

            final SelectorSettings sels = MiscToolItem.loadSelectorSettings();
            final ObjectMode om = WorkingTypeToolItem.getWorkingType();
            if (om == ObjectMode.FACES) {
                if (sels.isTriangles() && !sels.isQuads()) {
                    sb.append(I18n.E3D_ONLY_TRIANGLES);
                } else if (sels.isQuads() && !sels.isTriangles()) {
                    sb.append(I18n.E3D_ONLY_QUADS);
                }
            } else if (om == ObjectMode.LINES) {
                if (sels.isLines() && !sels.isCondlines()) {
                    sb.append(I18n.E3D_ONLY_LINES);
                } else if (sels.isCondlines() && !sels.isLines()) {
                    sb.append(I18n.E3D_ONLY_CONDLINES);
                }
            }

            if (AddToolItem.isAddingSomething() && !AddToolItem.isAddingVertices() && !AddToolItem.isAddingSubfiles()) {
                if (AddToolItem.isAddingDistance()|| AddToolItem.isAddingLines()) {
                    Vector4f cur = c3d.getCursorSnapped3D();
                    Vertex v = c3d.getLockableDatFileReference().getNearestObjVertex1();
                    if (v != null) {
                        float distLDU = Vector4f.sub(v.toVector4f(), cur, null).length() / 1000f;
                        float distMM = distLDU * 0.4f;
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(I18n.C3D_LENGTH);
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(DF4F.format(distLDU));
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(I18n.UNITS_LDU);
                        sb.append(" | "); //$NON-NLS-1$
                        sb.append(DF2F.format(distMM));
                        sb.append(" "); //$NON-NLS-1$
                        sb.append(I18n.UNITS_SECONDARY);
                    }
                }

                sb.append(" "); //$NON-NLS-1$
                sb.append(I18n.E3D_ADD_FREEZE_HINT);
            }

            showAndCheckImageCalibrationHint(sb, df);

            if (Math.abs(CSG.timeOfLastOptimization - System.currentTimeMillis()) < 10000) {
                sb.append(" "); //$NON-NLS-1$
                sb.append(I18n.E3D_CSG_OPTIMISATION);
                sb.append(" "); //$NON-NLS-1$
                sb.append(DF2F.format(CSG.globalOptimizationRate));
                sb.append("%"); //$NON-NLS-1$
            }

            if (MathHelper.getPreciseSnap()) {
                sb.append(", "); //$NON-NLS-1$
                sb.append(I18n.E3D_ROUND_SNAP_TOO_SMALL);
            }

            Editor3DWindow.getStatusLabel().setText(sb.toString());
            Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));

            VertexWindow.placeVertexWindow();

        } catch (SWTException swtex) {
            NLogger.debug(GuiStatusManager.class, "Uncritical SWTExecption. Widget is disposed."); //$NON-NLS-1$
            NLogger.debug(GuiStatusManager.class, swtex);
        }
    }

    private static void showAndCheckImageCalibrationHint(final StringBuilder sb, final DatFile df) {
        if (Editor3DWindow.getWindow().isCalibratePngPicture()) {
            final VertexManager vm = df.getVertexManager();
            sb.append(" "); //$NON-NLS-1$
            sb.append(I18n.CALIBRATE_DRAW_LINE);

            // This is not the cleanest way, since a value dialog will be triggered, when the status should be displayed,
            // but anyhow. BG Image Calibration is not the daily task of a part author, done every second.
            final int selectedLineCount = vm.getSelectedLines().size();
            final int selectedCondlineCount = vm.getSelectedCondlines().size();
            if (selectedCondlineCount + selectedLineCount == 1) {
                GData selectedLine = null;
                if (selectedLineCount == 1) {
                    try {
                        GData2 line = vm.getSelectedLines().iterator().next();
                        selectedLine = line;
                    } catch (NoSuchElementException consumed) {
                        NLogger.debug(GuiStatusManager.class, consumed);
                    }
                }

                if (selectedCondlineCount == 1) {
                    try {
                        GData5 condline = vm.getSelectedCondlines().iterator().next();
                        selectedLine = condline;
                    } catch (NoSuchElementException consumed) {
                        NLogger.debug(GuiStatusManager.class, consumed);
                    }
                }

                if (selectedLine != null) {
                    Set<VertexInfo> vis = vm.getLineLinkedToVertices().get(selectedLine);
                    if (vis != null && vis.size() > 1) {
                        vm.getSelectedCondlines().clear();
                        vm.getSelectedLines().clear();
                        CalibrateDialog dialog = new CalibrateDialog(Editor3DWindow.getWindow().getShell(), df, vis);
                        if (dialog.open() == IDialogConstants.OK_ID) {
                            dialog.performCalibration();
                        }

                        Editor3DWindow.getWindow().setCalibratePngPicture(false);
                        vm.clearSelection();
                    }
                }
            }
        }
    }

    private static Iterator<ScreenVertex> getVerticesOrderedByScreenCoordinate(Iterator<Vertex> vsi, PerspectiveCalculator pc) {
        final SortedSet<ScreenVertex> result = new TreeSet<>();
        try {
            while (vsi.hasNext()) {
                Vertex v = vsi.next();
                result.add(new ScreenVertex(v, pc.getScreenCoordinatesFrom3D(v.xp, v.yp, v.zp)));
            }
        } catch (NoSuchElementException consumed) {
            NLogger.debug(GuiStatusManager.class, consumed);
        }
        return result.iterator();
    }

    private static void formatForOneSelectedVertex(final StringBuilder sb, final Iterator<ScreenVertex> vsi) {
        try {
            Vertex v = vsi.next().getVertex3D();
            sb.append(" " + I18n.C3D_SELECTED_VERTEX + " @  ["); //$NON-NLS-1$ //$NON-NLS-2$
            sb.append(DF4F.format(v.xp.multiply(View.unitFactor)));
            sb.append("; "); //$NON-NLS-1$
            sb.append(DF4F.format(v.yp.multiply(View.unitFactor)));
            sb.append("; "); //$NON-NLS-1$
            sb.append(DF4F.format(v.zp.multiply(View.unitFactor)));
            sb.append("]"); //$NON-NLS-1$
        } catch (NoSuchElementException consumed) {
            NLogger.debug(GuiStatusManager.class, consumed);
        }
    }

    private static void formatForTwoSelectedVertices(final StringBuilder sb, final Iterator<ScreenVertex> vsi) {
        try {
            Vertex v = vsi.next().getVertex3D();
            sb.append("["); //$NON-NLS-1$
            sb.append(DF2F.format(v.xp.multiply(View.unitFactor)));
            sb.append("; "); //$NON-NLS-1$
            sb.append(DF2F.format(v.yp.multiply(View.unitFactor)));
            sb.append("; "); //$NON-NLS-1$
            sb.append(DF2F.format(v.zp.multiply(View.unitFactor)));
            sb.append("]"); //$NON-NLS-1$
        } catch (NoSuchElementException consumed) {
            NLogger.debug(GuiStatusManager.class, consumed);
        }
    }

    private static void formatForThreeSelectedVertices(final StringBuilder sb, final Iterator<ScreenVertex> vsi) {
        try {
            Vertex v = vsi.next().getVertex3D();
            sb.append("["); //$NON-NLS-1$
            sb.append(DF1F.format(v.xp.multiply(View.unitFactor)));
            sb.append(";"); //$NON-NLS-1$
            sb.append(DF1F.format(v.yp.multiply(View.unitFactor)));
            sb.append(";"); //$NON-NLS-1$
            sb.append(DF1F.format(v.zp.multiply(View.unitFactor)));
            sb.append("]"); //$NON-NLS-1$
        } catch (NoSuchElementException consumed) {
            NLogger.debug(GuiStatusManager.class, consumed);
        }
    }

    private static void formatForFourSelectedVertices(final StringBuilder sb, final Iterator<ScreenVertex> vsi) {
        try {
            Vertex v = vsi.next().getVertex3D();
            sb.append("["); //$NON-NLS-1$
            sb.append(DF0F.format(v.xp.multiply(View.unitFactor)));
            sb.append(";"); //$NON-NLS-1$
            sb.append(DF0F.format(v.yp.multiply(View.unitFactor)));
            sb.append(";"); //$NON-NLS-1$
            sb.append(DF0F.format(v.zp.multiply(View.unitFactor)));
            sb.append("]"); //$NON-NLS-1$
        } catch (NoSuchElementException consumed) {
            NLogger.debug(GuiStatusManager.class, consumed);
        }
    }

    public static synchronized void updateStatus(DatFile df) {
        final StringBuilder sb = new StringBuilder();
        final VertexManager vm = df.getVertexManager();

        sb.append(vm.getSlantingMatrixProjectorStatusString());

        updateSelection(sb, vm);

        sb.append(" "); //$NON-NLS-1$
        sb.append(df.getShortNameMixedCase());
        Editor3DWindow.getStatusLabel().setText(sb.toString());
        Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    public static void updateStatus() {
        Editor3DWindow.getStatusLabel().setText(I18n.E3D_NO_FILE_SELECTED);
        Editor3DWindow.getStatusLabel().setSize(Editor3DWindow.getStatusLabel().computeSize(SWT.DEFAULT, SWT.DEFAULT));
        Editor3DWindow.getStatusLabel().update();
    }

    public static void updateSelection(StringBuilder sb, VertexManager vm) {
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
            needsComma = appendSelectionInfo(sb, I18n.C3D_SELECTED_VERTEX, I18n.C3D_SELECTED_VERTICES, selectedVerticesCount, needsComma);
            needsComma = appendSelectionInfo(sb, I18n.C3D_SELECTED_LINE, I18n.C3D_SELECTED_LINES, selectedLineCount, needsComma);
            needsComma = appendSelectionInfo(sb, I18n.C3D_SELECTED_TRIANGLE, I18n.C3D_SELECTED_TRIANGLES, selectedTriangleCount, needsComma);
            needsComma = appendSelectionInfo(sb, I18n.C3D_SELECTED_QUAD, I18n.C3D_SELECTED_QUADS, selectedQuadCount, needsComma);
            needsComma = appendSelectionInfo(sb, I18n.C3D_SELECTED_CONDLINE, I18n.C3D_SELECTED_CONDLINES, selectedCondlineCount, needsComma);
            appendSelectionInfo(sb, I18n.C3D_SELECTED_SUBFILE, I18n.C3D_SELECTED_SUBFILES, selectedSubfileCount, needsComma);
            sb.append(") "); //$NON-NLS-1$

            if (selectedSubfileCount == 1) {
                updateSubfileSelection(sb, vm);
            }
        }
    }

    private static void updateSubfileSelection(StringBuilder sb, VertexManager vm) {
        try {
            Iterator<GData1> si = vm.getSelectedSubfiles().iterator();
            GData1 s = si.next();
            sb.append(" "); //$NON-NLS-1$
            sb.append(s.getShortName());
            sb.append(" @ "); //$NON-NLS-1$
        } catch (Exception consumed) {
            NLogger.debug(GuiStatusManager.class, consumed);
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
        return doAppend || needsComma;
    }
}
