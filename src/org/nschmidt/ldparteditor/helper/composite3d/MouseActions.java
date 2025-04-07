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

import java.io.File;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.ScalableComposite;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTabState;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.DatType;
import org.nschmidt.ldparteditor.data.GColour;
import org.nschmidt.ldparteditor.data.GData;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.GDataCSG;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.ParsingResult;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.LDConfig;
import org.nschmidt.ldparteditor.enumtype.ManipulatorAxisMode;
import org.nschmidt.ldparteditor.enumtype.ManipulatorScope;
import org.nschmidt.ldparteditor.enumtype.MouseButton;
import org.nschmidt.ldparteditor.enumtype.Threshold;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.enumtype.WorkingMode;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.LDPartEditorException;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.helper.WidgetSelectionHelper;
import org.nschmidt.ldparteditor.helper.math.MathHelper;
import org.nschmidt.ldparteditor.helper.math.MatrixOperations;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.AddToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.ManipulatorScopeToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.TransformationModeToolItem;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.WorkingTypeToolItem;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.text.DatParser;
import org.nschmidt.ldparteditor.vertexwindow.VertexWindow;
import org.nschmidt.ldparteditor.widget.Tree;
import org.nschmidt.ldparteditor.widget.TreeItem;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Implementations of the actions which will be performed on
 * {@linkplain Composite3D} by {@code SWT.MouseDown}, {@code SWT.MouseMove} and
 * {@code SWT.MouseUp}
 */
public class MouseActions {

    private static final double PI_8TH = Math.PI / 8d;

    private static final long[] lastTextureGC = new long[] { System.currentTimeMillis() };

    public static final int MOUSE_LAYOUT_DEFAULT = 0;
    public static final int MOUSE_LAYOUT_SWITCH_ROTATE_AND_TRANSLATE = 1;

    // Non-public properties
    /** The 3D Composite [NOT PUBLIC YET] */
    private final Composite3D c3d;
    /**
     * The cursor position of the mouse recognized by the MouseDown event [NOT
     * PUBLIC YET]
     */
    private final Vector2f oldMousePosition = new Vector2f();
    /** The old translation matrix of the view [NOT PUBLIC YET] */
    private final Matrix4f oldViewportTranslation = new Matrix4f();
    /** The old rotation matrix of the view [NOT PUBLIC YET] */
    private Matrix4f oldViewportRotation = new Matrix4f();
    /** Greater {@code 0} if the mouse button is down [NOT PUBLIC YET] */
    private int mouseButtonPressed;

    private boolean translateAtSelect = false;
    private BigDecimal aXx = null;
    private BigDecimal aXy = null;
    private BigDecimal aXz = null;
    private BigDecimal aYx = null;
    private BigDecimal aYy = null;
    private BigDecimal aYz = null;
    private Vector4f mX = null;
    private Vector4f mY = null;

    public MouseActions(Composite3D c3d) {
        this.c3d = c3d;
    }

    // MARK MouseDown
    /**
     * Triggered actions on {@code SWT.MouseDown}
     *
     * @param event
     *            Event data.
     */
    @SuppressWarnings("java:S2111")
    public void mouseDown(Event event) {
        syncManipulator();
        c3d.getManipulator().lock();

        final DatFile datfile = c3d.getLockableDatFileReference();
        if (!datfile.isDrawSelection()) return;

        // The user can switch rotation and translation
        reMapMouseEvent(event);

        final VertexManager vm = datfile.getVertexManager();
        vm.addSnapshot();
        vm.getResetTimer().set(true);
        datfile.setLastSelectedComposite(c3d);

        mouseButtonPressed = event.button;

        oldMousePosition.set(event.x, event.y);
        switch (mouseButtonPressed) {
        case MouseButton.LEFT:
            if (c3d.isQuicklyTransforming()) break;
            final Editor3DWindow window = Editor3DWindow.getWindow();
            if ((event.stateMask & SWT.SHIFT) == SWT.SHIFT && !AddToolItem.isAddingSomething()) {
                Manipulator m = c3d.getManipulator();
                m.startTranslation2(c3d);
                translateAtSelect = true;
                mX = new Vector4f(m.getXaxis());
                mY = new Vector4f(m.getYaxis());
                BigDecimal[] aX = m.getAccurateXaxis();
                BigDecimal[] aY = m.getAccurateYaxis();
                aXx = aX[0];
                aXy = aX[1];
                aXz = aX[2];
                aYx = aY[0];
                aYy = aY[1];
                aYz = aY[2];
                m.getXaxis().set(c3d.getGenerator()[0]);
                m.getXaxis().normalise();
                m.getYaxis().set(c3d.getGenerator()[1]);
                m.getYaxis().normalise();
                m.setAccurateXaxis(new BigDecimal(m.getXaxis().x), new BigDecimal(m.getXaxis().y), new BigDecimal(m.getXaxis().z));
                m.setAccurateYaxis(new BigDecimal(m.getYaxis().x), new BigDecimal(m.getYaxis().y), new BigDecimal(m.getYaxis().z));
            } else  if (TransformationModeToolItem.getWorkingAction() == WorkingMode.SELECT || AddToolItem.isAddingSomething()) {
                c3d.setDoingSelection(true);
            } else  if (!AddToolItem.isAddingSomething()) {
                c3d.getManipulator().startTranslation(c3d);
            }
            c3d.getOldMousePosition().set(event.x, event.y);
            Vector4f cursorCoordinates = c3d.getPerspectiveCalculator().get3DCoordinatesFromScreen(event.x, event.y);
            c3d.getSelectionStart().set(cursorCoordinates);
            if (vm.getSelectedBgPicture() != null) {
                vm.setSelectedBgPicture(null);
                Editor3DWindow.getWindow().updateBgPictureTab();
            }
            if (AddToolItem.isAddingSomething() && !datfile.isReadOnly()) {
                if (!vm.getSelectedData().isEmpty()) {
                    vm.clearSelection();
                }
                if (AddToolItem.isAddingVertices()) {
                    GData vertexLine = vm.addVertex(new Vertex(c3d.getCursorSnapped3Dprecise()[0], c3d.getCursorSnapped3Dprecise()[1], c3d.getCursorSnapped3Dprecise()[2]));
                    if (vertexLine != null) {
                        vm.setModified(true, true);
                        if (!Project.getUnsavedFiles().contains(datfile)) {
                            Project.addUnsavedFile(datfile);
                            Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                        }
                        datfile.addToTailOrInsertAfterCursor(vertexLine);
                    }
                } else if (AddToolItem.isAddingSubfiles()) {
                    final Tree tree = window.getProjectParts().getParent();
                    if (tree.getSelectionCount() == 1) {
                        final TreeItem item = tree.getSelection()[0];
                        final Object obj = item.getData();
                        if (obj instanceof DatFile data) {
                            String ref = new File(data.getNewName()).getName();
                            if (data.getType().equals(DatType.SUBPART))
                                ref = "s\\" + ref; //$NON-NLS-1$
                            if (data.getType().equals(DatType.PRIMITIVE48))
                                ref = "48\\" + ref; //$NON-NLS-1$
                            if (data.getType().equals(DatType.PRIMITIVE8))
                                ref = "8\\" + ref; //$NON-NLS-1$
                            final BigDecimal[] cur = c3d.getCursorSnapped3Dprecise();

                            Set<String> alreadyParsed = new HashSet<>();
                            alreadyParsed.add(datfile.getShortName());
                            final GColour col16 = LDConfig.getColour16();
                            List<ParsingResult> subfileLine = DatParser
                                    .parseLine(
                                            "1 16 " + MathHelper.bigDecimalToString(cur[0]) + " " + MathHelper.bigDecimalToString(cur[1]) + " " + MathHelper.bigDecimalToString(cur[2]) + " 1 0 0 0 1 0 0 0 1 " + ref, -1, 0, col16.getR(), col16.getG(), col16.getB(), 1.1f, View.DUMMY_REFERENCE, View.ID, View.ACCURATE_ID, datfile, false, alreadyParsed); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            GData1 vertexLine = (GData1) subfileLine.get(0).getGraphicalData();
                            if (vertexLine != null) {
                                datfile.addToTailOrInsertAfterCursor(vertexLine);
                                datfile.getVertexManager().setModified(true, true);
                                if (!Project.getUnsavedFiles().contains(datfile)) {
                                    Project.addUnsavedFile(datfile);
                                    Editor3DWindow.getWindow().updateTreeUnsavedEntries();
                                }
                                datfile.getVertexManager().validateState();
                            }
                            AddToolItem.unselectAddSubfile();
                        }
                    }
                }
            }

            break;
        case MouseButton.MIDDLE:
            c3d.getCanvas().forceFocus();
            Project.setFileToEdit(datfile);
            if (c3d.isDoingSelection())
                break;
            Matrix4f.load(c3d.getRotation(), oldViewportRotation);
            break;
        case MouseButton.RIGHT:
            c3d.getCanvas().forceFocus();
            vm.setSkipSyncWithTextEditor(true);
            Project.setFileToEdit(datfile);
            if (c3d.isDoingSelection())
                break;
            Matrix4f.load(c3d.getTranslation(), oldViewportTranslation);
            break;
        default:
            break;
        }

        syncManipulator();
    }

    /**
     * Triggered actions on {@code SWT.MouseMove}
     *
     * @param event
     *            Event data.
     */
    // MARK MouseMove
    private boolean triedHGfix = false;

    @SuppressWarnings("java:S2111")
    public void mouseMove(Event event) {
        if (NLogger.debugging && c3d.isQuicklyTransforming()) {
            // Debug info for quick transform
            Manipulator m = c3d.getManipulator();
            NLogger.debug(getClass(),"---START MANIPULATOR STATUS---"); //$NON-NLS-1$
            NLogger.debug(getClass(),"xTranslate: " + m.isXtranslate()); //$NON-NLS-1$
            NLogger.debug(getClass(),"yTranslate: " + m.isYtranslate()); //$NON-NLS-1$
            NLogger.debug(getClass(),"zTranslate: " + m.isZtranslate()); //$NON-NLS-1$

            NLogger.debug(getClass(),"xRotate: " + m.isXrotate()); //$NON-NLS-1$
            NLogger.debug(getClass(),"yRotate: " + m.isYrotate()); //$NON-NLS-1$
            NLogger.debug(getClass(),"zRotate: " + m.isZrotate()); //$NON-NLS-1$
            NLogger.debug(getClass(),"vRotate: " + m.isVrotate()); //$NON-NLS-1$

            NLogger.debug(getClass(),"xScale: " + m.isXscale()); //$NON-NLS-1$
            NLogger.debug(getClass(),"yScale: " + m.isYscale()); //$NON-NLS-1$
            NLogger.debug(getClass(),"zScale: " + m.isZscale()); //$NON-NLS-1$

            NLogger.debug(getClass(),"position: " + m.getPosition()); //$NON-NLS-1$
            NLogger.debug(getClass(),"---END MANIPULATOR STATUS---"); //$NON-NLS-1$
        }

        DatFile.setLastHoveredComposite(c3d);
        if (!c3d.getLockableDatFileReference().isDrawSelection()) return;

        VertexWindow.placeVertexWindow();
        checkKeyStatesOfEvent(event, SWT.COMMAND);
        checkKeyStatesOfEvent(event, SWT.CTRL);
        checkKeyStatesOfEvent(event, SWT.SHIFT);
        checkKeyStatesOfEvent(event, SWT.ALT);

        {
            long ct = System.currentTimeMillis();
            if (ct > 25000 + lastTextureGC[0]) {
                c3d.getRenderer().disposeOldTextures();
                lastTextureGC[0] = ct;
            }
        }

        ViewIdleManager.refresh(c3d.getCanvas(), c3d.getRenderer());
        Point cSize = c3d.getSize();
        c3d.getMousePosition().set(event.x, event.y);
        KeyStateManager keyboard = c3d.getKeys();
        PerspectiveCalculator perspective = c3d.getPerspectiveCalculator();
        Matrix4f viewportTranslation = c3d.getTranslation();
        Matrix4f viewportRotation = c3d.getRotation();
        float viewportPixelPerLDU = c3d.getViewportPixelPerLDU();
        final BigDecimal[] manipulatorSnap = Manipulator.getSnap();
        if (c3d.isQuicklyTransforming() && mouseButtonPressed != MouseButton.LEFT && !AddToolItem.isAddingSomething()) {
            // Since quick transform was triggered via keyboard, we need an additional step where the old mouse position is the current one (zero delta).
            if (!c3d.isQuicklyTransformingWarmup()) {
                oldMousePosition.set(event.x, event.y);
                c3d.setQuicklyTransformingWarmup(true);
            }

            Vector4f temp;
            if (translateAtSelect) {
                temp = c3d.getManipulator().transformAtSelect(oldMousePosition, event.x, event.y, c3d);
            } else {
                temp = c3d.getManipulator().transform(oldMousePosition, event.x, event.y, c3d);
            }
            if (ManipulatorScopeToolItem.getTransformationScope() == ManipulatorScope.GLOBAL) {
                c3d.getManipulator().getPosition().set(temp);
                c3d.getManipulator().setAccuratePosition(new BigDecimal(temp.x / 1000f), new BigDecimal(temp.y / 1000f), new BigDecimal(temp.z / 1000f));
            }

            c3d.getVertexManager().getResetTimer().set(true);
        }
        switch (mouseButtonPressed) {
        case MouseButton.LEFT:
            Vector4f temp;
            if (!AddToolItem.isAddingSomething()) {
                if (translateAtSelect) {
                    temp = c3d.getManipulator().transformAtSelect(oldMousePosition, event.x, event.y, c3d);
                } else {
                    temp = c3d.getManipulator().transform(oldMousePosition, event.x, event.y, c3d);
                }
                if (ManipulatorScopeToolItem.getTransformationScope() == ManipulatorScope.GLOBAL) {
                    c3d.getManipulator().getPosition().set(temp);
                    c3d.getManipulator().setAccuratePosition(new BigDecimal(temp.x / 1000f), new BigDecimal(temp.y / 1000f), new BigDecimal(temp.z / 1000f));
                }
            }
            c3d.getVertexManager().getResetTimer().set(true);
            break;
        case MouseButton.MIDDLE:
            if (c3d.isDoingSelection() || c3d.getPerspectiveCalculator().hasRotationLock())
                break;
            if (c3d.isClassicPerspective()) {
                c3d.setClassicPerspective(false);
                if (!c3d.getViewAnglesMenu().isDisposed()) {
                    WidgetSelectionHelper.unselectAllChildButtons(c3d.getViewAnglesMenu());
                }
            }
            float rx = 0;
            float ry = 0;

            final boolean snapXaxis = keyboard.isAltPressed();
            final boolean snapYaxis = keyboard.isShiftPressed();
            final boolean snapZaxis = keyboard.isCtrlPressed();

            if (snapZaxis) {
                if (c3d.hasNegDeterminant()) {
                    rx = (float) (Math.atan2(-cSize.y / 2f + oldMousePosition.y, -cSize.x / 2f + oldMousePosition.x)
                            - Math.atan2(-cSize.y / 2f + event.y, -cSize.x / 2f + event.x));
                } else {
                    rx = (float) (Math.atan2(cSize.y / 2f - oldMousePosition.y, cSize.x / 2f - oldMousePosition.x)
                            - Math.atan2(cSize.y / 2f - event.y, cSize.x / 2f - event.x));
                }
                Vector4f xAxis4fRotation = new Vector4f(0f, 0f, 1.0f, 1.0f);
                Matrix4f ovrInverse = Matrix4f.invert(oldViewportRotation, null);
                Matrix4f.transform(ovrInverse, xAxis4fRotation, xAxis4fRotation);
                Vector3f xAxis3fRotation = new Vector3f(xAxis4fRotation.x, xAxis4fRotation.y, xAxis4fRotation.z);
                Matrix4f.rotate(snap(rx, manipulatorSnap), xAxis3fRotation, oldViewportRotation, viewportRotation);
            } else {
                if (c3d.hasNegDeterminant()) {
                    rx = (event.x - oldMousePosition.x) / cSize.x * (float) Math.PI;
                    ry = (event.y - oldMousePosition.y) / cSize.y * (float) Math.PI;
                } else {
                    rx = (oldMousePosition.x - event.x) / cSize.x * (float) Math.PI;
                    ry = (oldMousePosition.y - event.y) / cSize.y * (float) Math.PI;
                }
                Vector4f xAxis4fRotation = new Vector4f(1.0f, 0f, 0f, 1.0f);
                Vector4f yAxis4fRotation = new Vector4f(0f, 1.0f, 0f, 1.0f);
                Matrix4f ovrInverse = Matrix4f.invert(oldViewportRotation, null);
                Matrix4f.transform(ovrInverse, xAxis4fRotation, xAxis4fRotation);
                Matrix4f.transform(ovrInverse, yAxis4fRotation, yAxis4fRotation);
                Vector3f xAxis3fRotation = new Vector3f(xAxis4fRotation.x, xAxis4fRotation.y, xAxis4fRotation.z);
                Vector3f yAxis3fRotation = new Vector3f(yAxis4fRotation.x, yAxis4fRotation.y, yAxis4fRotation.z);

                if (snapXaxis) {
                    Matrix4f.rotate(snap(rx, manipulatorSnap), yAxis3fRotation, oldViewportRotation, viewportRotation);
                } else if (snapYaxis) {
                    Matrix4f.rotate(snap(ry, manipulatorSnap), xAxis3fRotation, oldViewportRotation, viewportRotation);
                } else {
                    Matrix4f.rotate(rx, yAxis3fRotation, oldViewportRotation, viewportRotation);
                    Matrix4f.rotate(ry, xAxis3fRotation, viewportRotation, viewportRotation);
                }
            }
            perspective.calculateOriginData();
            c3d.getVertexManager().getResetTimer().set(true);
            break;
        case MouseButton.RIGHT:
            if (c3d.isDoingSelection())
                break;
            float dx = 0;
            float dy = 0;
            if (!(keyboard.isShiftPressed() || Cocoa.IS_COCOA && keyboard.isAltPressed()) ) {
                dx = (oldMousePosition.x - event.x) / viewportPixelPerLDU;
            }
            if (!(keyboard.isCtrlPressed() || Cocoa.IS_COCOA && keyboard.isCmdPressed()) || AddToolItem.isAddingSomething()) {
                dy = (event.y - oldMousePosition.y) / viewportPixelPerLDU;
            }
            translateViewport(dx, dy, viewportTranslation, viewportRotation, perspective);
            c3d.getVertexManager().getResetTimer().set(true);
            break;
        default:
            break;
        }
        c3d.getCursor3D().set(perspective.get3DCoordinatesFromScreen(event.x, event.y));
        final BigDecimal snapPreciseX = manipulatorSnap[0];
        final BigDecimal snapPreciseY = manipulatorSnap[1];
        final BigDecimal snapPreciseZ = manipulatorSnap[2];
        final float snapX = snapPreciseX.floatValue() * 1000f;
        final float snapY = snapPreciseY.floatValue() * 1000f;
        final float snapZ = snapPreciseZ.floatValue() * 1000f;
        final float sx = c3d.getCursor3D().x;
        final float sy = c3d.getCursor3D().y;
        final float sz = c3d.getCursor3D().z;

        c3d.getCursorSnapped3Dprecise()[0] = new BigDecimal(sx / 1000f).subtract(new BigDecimal(sx / 1000f).remainder(snapPreciseX, Threshold.MC));
        c3d.getCursorSnapped3Dprecise()[1] = new BigDecimal(sy / 1000f).subtract(new BigDecimal(sy / 1000f).remainder(snapPreciseY, Threshold.MC));
        c3d.getCursorSnapped3Dprecise()[2] = new BigDecimal(sz / 1000f).subtract(new BigDecimal(sz / 1000f).remainder(snapPreciseZ, Threshold.MC));

        c3d.getCursorSnapped3D().set(sx - sx % snapX, sy - sy % snapY, sz - sz % snapZ, 1f);

        c3d.getScreenXY().set(event.x, event.y);
        GuiStatusManager.updateStatus(c3d);
        ((ScalableComposite) c3d.getParent()).redrawScales(event.x, event.y);
        if (AddToolItem.isAddingSomething() && !c3d.isDoingSelection()) {
            if (AddToolItem.isAddingLines() || AddToolItem.isAddingDistance()) {
                Vertex v1 = c3d.getLockableDatFileReference().getObjVertex1();
                if (v1 != null) {
                    if (keyboard.isShiftPressed()) {
                        Vertex nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVertexToLineEnd(new Vertex(c3d.getCursor3D()));
                        c3d.getLockableDatFileReference().setObjVertex1(nv);
                        c3d.getLockableDatFileReference().setObjVertex2(nv);
                        c3d.getCursorSnapped3D().set(nv.x, nv.y, nv.z, 1f);
                        c3d.setCursorSnapped3Dprecise(nv.xp, nv.yp, nv.zp);
                    } else if (!keyboard.isCtrlPressed()) {
                        Vertex v2 = c3d.getLockableDatFileReference().getObjVertex2();
                        Vector3f v13 = new Vector3f(v1.x, v1.y, v1.z);
                        Vector3f v23 = new Vector3f(v2.x, v2.y, v2.z);
                        Vector4f cu3d = c3d.getCursorSnapped3D();
                        Vector3f v3d = new Vector3f(cu3d.x, cu3d.y, cu3d.z);
                        c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                        if (Vector3f.sub(v13, v3d, null).lengthSquared() < Vector3f.sub(v23, v3d, null).lengthSquared()) {
                            c3d.getLockableDatFileReference().setNearestObjVertex1(v1);
                            if (v1 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v1);
                        } else {
                            c3d.getLockableDatFileReference().setNearestObjVertex1(v2);
                            if (v2 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v2);
                        }
                    }
                }
            } else if (AddToolItem.isAddingTriangles() || AddToolItem.isAddingProtractor()) {
                Vertex v1 = c3d.getLockableDatFileReference().getObjVertex1();
                Vertex v2 = c3d.getLockableDatFileReference().getObjVertex2();
                Vertex v3 = c3d.getLockableDatFileReference().getObjVertex3();
                if (v1 != null) {
                    if (v2 != null) {
                        if (v3 != null) {
                            if (keyboard.isShiftPressed()) {
                                Object[] nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVerticesToLines(new Vertex(c3d.getCursor3D()), true);
                                c3d.getLockableDatFileReference().setObjVertex1((Vertex) nv[0]);
                                c3d.getLockableDatFileReference().setObjVertex2((Vertex) nv[1]);
                                c3d.getLockableDatFileReference().setObjVertex3(null);
                                c3d.getLockableDatFileReference().setNearestObjVertex1((Vertex) nv[0]);
                                c3d.getLockableDatFileReference().setNearestObjVertex2((Vertex) nv[1]);
                                c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                                if (nv[0] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add((Vertex) nv[0]);
                                if (nv[1] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add((Vertex) nv[1]);
                            } else if (!keyboard.isCtrlPressed()) {
                                Vertex v3d = new Vertex(c3d.getCursorSnapped3Dprecise());
                                c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                                Vertex[] v = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVerticesToTriangleEdges(v3d, v1, v2, v3, c3d);
                                c3d.getLockableDatFileReference().setNearestObjVertex1(v[0]);
                                c3d.getLockableDatFileReference().setNearestObjVertex2(v[1]);
                                if (v[0] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v[0]);
                                if (v[1] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v[1]);
                            }
                        } else {
                            if (keyboard.isShiftPressed()) {
                                Object[] nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVerticesToLines(new Vertex(c3d.getCursor3D()), true);
                                c3d.getLockableDatFileReference().setObjVertex1((Vertex) nv[0]);
                                c3d.getLockableDatFileReference().setObjVertex2((Vertex) nv[1]);
                                c3d.getLockableDatFileReference().setObjVertex3(null);
                                c3d.getLockableDatFileReference().setNearestObjVertex1((Vertex) nv[0]);
                                c3d.getLockableDatFileReference().setNearestObjVertex2((Vertex) nv[1]);
                                c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                                if (nv[0] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add((Vertex) nv[0]);
                                if (nv[1] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add((Vertex) nv[1]);
                            } else if (!keyboard.isCtrlPressed()) {
                                c3d.getLockableDatFileReference().setNearestObjVertex2(v2);
                                if (v1 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v1);
                                if (v2 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v2);
                            }
                        }
                    } else {
                        if (keyboard.isShiftPressed()) {
                            Vertex nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVertexToLineEnd(new Vertex(c3d.getCursor3D()));
                            c3d.getLockableDatFileReference().setObjVertex1(nv);
                            c3d.getCursorSnapped3D().set(nv.x, nv.y, nv.z, 1f);
                        } else {
                            c3d.getLockableDatFileReference().setNearestObjVertex1(v1);
                            if (v1 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v1);
                        }
                    }
                }
            } else if (AddToolItem.isAddingQuads()) {
                Vertex v1 = c3d.getLockableDatFileReference().getObjVertex1();
                Vertex v2 = c3d.getLockableDatFileReference().getObjVertex2();
                Vertex v3 = c3d.getLockableDatFileReference().getObjVertex3();
                Vertex v4 = c3d.getLockableDatFileReference().getObjVertex4();
                if (v1 != null) {
                    if (v2 != null) {
                        if (v3 != null && v4 != null) {
                            if (keyboard.isShiftPressed()) {
                                Object[] nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVerticesToLines(new Vertex(c3d.getCursor3D()), true);
                                c3d.getLockableDatFileReference().setObjVertex1((Vertex) nv[0]);
                                c3d.getLockableDatFileReference().setObjVertex2((Vertex) nv[1]);
                                c3d.getLockableDatFileReference().setObjVertex3(null);
                                c3d.getLockableDatFileReference().setObjVertex4(null);
                                c3d.getLockableDatFileReference().setNearestObjVertex1((Vertex) nv[0]);
                                c3d.getLockableDatFileReference().setNearestObjVertex2((Vertex) nv[1]);
                                c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                                if (nv[0] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add((Vertex) nv[0]);
                                if (nv[1] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add((Vertex) nv[1]);
                            } else if (!keyboard.isCtrlPressed()) {
                                Vertex v3d = new Vertex(c3d.getCursorSnapped3Dprecise());
                                c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                                Vertex[] tv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVerticesToTriangleEdges(v3d, v1, v2, v3, c3d);
                                Vertex[] tv2 = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVerticesToTriangleEdges(v3d, v3, v4, v1, c3d);
                                SortedSet<Vertex> qverts = new TreeSet<>();
                                qverts.add(tv[0]);
                                qverts.add(tv[1]);
                                qverts.add(tv2[0]);
                                qverts.add(tv2[1]);
                                if (qverts.size() == 3) {
                                    Iterator<Vertex> qi = qverts.iterator();
                                    Vertex[] nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVerticesToTriangleEdges(v3d, qi.next(), qi.next(), qi.next(), c3d);
                                    c3d.getLockableDatFileReference().setNearestObjVertex1(nv[0]);
                                    c3d.getLockableDatFileReference().setNearestObjVertex2(nv[1]);
                                    c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                                    if (nv[0] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(nv[0]);
                                    if (nv[1] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(nv[1]);
                                }
                            }
                        } else {
                            if (keyboard.isShiftPressed()) {
                                Object[] nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVerticesToLines(new Vertex(c3d.getCursor3D()), true);
                                c3d.getLockableDatFileReference().setObjVertex1((Vertex) nv[0]);
                                c3d.getLockableDatFileReference().setObjVertex2((Vertex) nv[1]);
                                c3d.getLockableDatFileReference().setObjVertex3(null);
                                c3d.getLockableDatFileReference().setObjVertex4(null);
                                c3d.getLockableDatFileReference().setNearestObjVertex1((Vertex) nv[0]);
                                c3d.getLockableDatFileReference().setNearestObjVertex2((Vertex) nv[1]);
                                c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                                if (nv[0] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add((Vertex) nv[0]);
                                if (nv[1] != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add((Vertex) nv[1]);
                            } else if (!keyboard.isCtrlPressed() && v3 == null) {
                                Vector3f v13 = new Vector3f(v1.x, v1.y, v1.z);
                                Vector3f v23 = new Vector3f(v2.x, v2.y, v2.z);
                                Vector4f cu3d = c3d.getCursorSnapped3D();
                                Vector3f v3d = new Vector3f(cu3d.x, cu3d.y, cu3d.z);
                                c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                                if (Vector3f.sub(v13, v3d, null).lengthSquared() > Vector3f.sub(v23, v3d, null).lengthSquared()) {
                                    c3d.getLockableDatFileReference().setNearestObjVertex1(v1);
                                    c3d.getLockableDatFileReference().setNearestObjVertex2(v2);
                                } else {
                                    c3d.getLockableDatFileReference().setNearestObjVertex1(v2);
                                    c3d.getLockableDatFileReference().setNearestObjVertex2(v1);
                                }
                                if (v1 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v1);
                                if (v2 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v2);
                            } else if (v3 != null) {
                                // Resolving Hourglass (no Coplanarity and
                                // Collinearity)
                                Vector4f cu3d = c3d.getCursorSnapped3D();
                                Vector3f v3d = new Vector3f(cu3d.x, cu3d.y, cu3d.z);
                                Vector3f v13f = new Vector3f(v1.x, v1.y, v1.z);
                                Vector3f v23f = new Vector3f(v2.x, v2.y, v2.z);
                                Vector3f v33f = new Vector3f(v3.x, v3.y, v3.z);
                                int cnc = 0;
                                Vector3f[] lineVectors = new Vector3f[4];
                                float[] normalDirections = new float[4];
                                Vector3f[] normals = new Vector3f[4];
                                lineVectors[0] = Vector3f.sub(v23f, v13f, null);
                                lineVectors[1] = Vector3f.sub(v33f, v23f, null);
                                lineVectors[2] = Vector3f.sub(v3d, v33f, null);
                                lineVectors[3] = Vector3f.sub(v13f, v3d, null);
                                normals[0] = Vector3f.cross(lineVectors[0], lineVectors[1], null);
                                normals[1] = Vector3f.cross(lineVectors[1], lineVectors[2], null);
                                normals[2] = Vector3f.cross(lineVectors[2], lineVectors[3], null);
                                normals[3] = Vector3f.cross(lineVectors[3], lineVectors[0], null);

                                Vector3f normal = new Vector3f();

                                for (int i = 0; i < 4; i++) {
                                    normalDirections[i] = MathHelper.directionOfVectors(normals[0], normals[i]);
                                    if (normalDirections[i] < 0)
                                        cnc++;
                                    normal = Vector3f.add(normals[i], normal, null);
                                }

                                if (cnc == 2) {
                                    // Hourglass
                                    if (triedHGfix) {
                                        triedHGfix = false;
                                        Vertex t;
                                        t = c3d.getLockableDatFileReference().getObjVertex3();
                                        c3d.getLockableDatFileReference().setObjVertex3(c3d.getLockableDatFileReference().getObjVertex1());
                                        c3d.getLockableDatFileReference().setObjVertex1(t);
                                    } else {
                                        triedHGfix = true;
                                        Vertex t;
                                        t = c3d.getLockableDatFileReference().getObjVertex1();
                                        c3d.getLockableDatFileReference().setObjVertex1(c3d.getLockableDatFileReference().getObjVertex2());
                                        c3d.getLockableDatFileReference().setObjVertex2(t);
                                        t = c3d.getLockableDatFileReference().getNearestObjVertex1();
                                        c3d.getLockableDatFileReference().setNearestObjVertex1(c3d.getLockableDatFileReference().getNearestObjVertex2());
                                        c3d.getLockableDatFileReference().setNearestObjVertex2(t);
                                    }
                                } else {
                                    triedHGfix = false;
                                }
                            }
                        }
                    } else {
                        if (keyboard.isShiftPressed()) {
                            Vertex nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVertexToLineEnd(new Vertex(c3d.getCursor3D()));
                            c3d.getLockableDatFileReference().setObjVertex1(nv);
                            c3d.getCursorSnapped3D().set(nv.x, nv.y, nv.z, 1f);
                        } else {
                            c3d.getLockableDatFileReference().setNearestObjVertex1(v1);
                            if (v1 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v1);
                        }
                    }
                }
            } else if (AddToolItem.isAddingCondlines()) {
                Vertex v1 = c3d.getLockableDatFileReference().getObjVertex1();
                Vertex v2 = c3d.getLockableDatFileReference().getObjVertex2();
                Vertex v3 = c3d.getLockableDatFileReference().getObjVertex3();
                Vertex v4 = c3d.getLockableDatFileReference().getObjVertex4();
                if (v1 != null) {
                    if (v2 != null) {
                        if (v3 != null && v4 != null) {
                            if (keyboard.isShiftPressed()) {
                                Vertex nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVertexToLineEnd(new Vertex(c3d.getCursor3D()));
                                c3d.getLockableDatFileReference().setObjVertex1(nv);
                                c3d.getLockableDatFileReference().setObjVertex2(nv);
                                c3d.getLockableDatFileReference().setObjVertex3(nv);
                                c3d.getLockableDatFileReference().setObjVertex4(nv);
                                c3d.getCursorSnapped3D().set(nv.x, nv.y, nv.z, 1f);
                            } else if (!keyboard.isCtrlPressed()) {
                                Vector3f v13 = new Vector3f(v1.x, v1.y, v1.z);
                                Vector3f v23 = new Vector3f(v2.x, v2.y, v2.z);
                                Vector4f cu3d = c3d.getCursorSnapped3D();
                                Vector3f v3d = new Vector3f(cu3d.x, cu3d.y, cu3d.z);
                                c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                                if (Vector3f.sub(v13, v3d, null).lengthSquared() < Vector3f.sub(v23, v3d, null).lengthSquared()) {
                                    c3d.getLockableDatFileReference().setNearestObjVertex1(v1);
                                    if (v1 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v1);
                                } else {
                                    c3d.getLockableDatFileReference().setNearestObjVertex1(v2);
                                    if (v2 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v2);
                                }
                            }
                        } else {
                            if (keyboard.isShiftPressed()) {
                                Vertex nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVertexToLineEnd(new Vertex(c3d.getCursor3D()));
                                c3d.getLockableDatFileReference().setObjVertex1(nv);
                                c3d.getLockableDatFileReference().setObjVertex2(nv);
                                c3d.getLockableDatFileReference().setObjVertex3(nv);
                                c3d.getLockableDatFileReference().setObjVertex4(nv);
                                c3d.getCursorSnapped3D().set(nv.x, nv.y, nv.z, 1f);
                            } else if (!keyboard.isCtrlPressed() && v3 == null) {
                                Vector3f v13 = new Vector3f(v1.x, v1.y, v1.z);
                                Vector3f v23 = new Vector3f(v2.x, v2.y, v2.z);
                                Vector4f cu3d = c3d.getCursorSnapped3D();
                                Vector3f v3d = new Vector3f(cu3d.x, cu3d.y, cu3d.z);
                                c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().clear();
                                if (Vector3f.sub(v13, v3d, null).lengthSquared() > Vector3f.sub(v23, v3d, null).lengthSquared()) {
                                    c3d.getLockableDatFileReference().setNearestObjVertex1(v1);
                                    c3d.getLockableDatFileReference().setNearestObjVertex2(v2);
                                } else {
                                    c3d.getLockableDatFileReference().setNearestObjVertex1(v2);
                                    c3d.getLockableDatFileReference().setNearestObjVertex2(v1);
                                }
                                if (v1 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v1);
                                if (v2 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v2);
                            }
                        }
                    } else {
                        if (keyboard.isShiftPressed()) {
                            Vertex nv = c3d.getLockableDatFileReference().getVertexManager().getMinimalDistanceVertexToLineEnd(new Vertex(c3d.getCursor3D()));
                            c3d.getLockableDatFileReference().setObjVertex1(nv);
                            c3d.getCursorSnapped3D().set(nv.x, nv.y, nv.z, 1f);
                        } else {
                            c3d.getLockableDatFileReference().setNearestObjVertex1(v1);
                            if (v1 != null) c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().add(v1);
                        }
                    }
                }
            }
        }

        syncManipulator();

        if (c3d.isSyncTranslation()) {
            float tx = c3d.getTranslation().m30;
            float ty = c3d.getTranslation().m31;
            float tz = c3d.getTranslation().m32;
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d2 = renderer.getC3D();
                if (!c3d2.isDisposed() && c3d != c3d2 && c3d.getLockableDatFileReference().equals(c3d2.getLockableDatFileReference())) {
                    c3d2.getTranslation().m30 = tx;
                    c3d2.getTranslation().m31 = ty;
                    c3d2.getTranslation().m32 = tz;
                    ((ScalableComposite) c3d2.getParent()).redrawScales();
                    c3d2.getPerspectiveCalculator().initializeViewportPerspective();
                }
            }
        }
    }

    private void checkKeyStatesOfEvent(final Event event, final int key) {
        c3d.getKeys().setKeyState(key, (event.stateMask & key) == key);
    }

    private float snap(float value, BigDecimal[] manipulatorSnap) {
        final float snap = (float) Math.min(manipulatorSnap[3].floatValue(), PI_8TH);
        return value - value % snap;
    }

    public void prepareTranslateViewport() {
        Matrix4f.load(c3d.getTranslation(), oldViewportTranslation);
    }

    public void translateViewport(float dx, float dy, Matrix4f viewportTranslation, Matrix4f viewportRotation,
            PerspectiveCalculator perspective) {
        Vector4f xAxis4fTranslation = new Vector4f(dx, 0, 0, 1.0f);
        Vector4f yAxis4fTranslation = new Vector4f(0, dy, 0, 1.0f);
        Matrix4f ovrInverse2 = Matrix4f.invert(viewportRotation, null);
        Matrix4f.transform(ovrInverse2, xAxis4fTranslation, xAxis4fTranslation);
        Matrix4f.transform(ovrInverse2, yAxis4fTranslation, yAxis4fTranslation);
        Vector3f xAxis3 = new Vector3f(xAxis4fTranslation.x, xAxis4fTranslation.y, xAxis4fTranslation.z);
        Vector3f yAxis3 = new Vector3f(yAxis4fTranslation.x, yAxis4fTranslation.y, yAxis4fTranslation.z);
        Matrix4f.load(oldViewportTranslation, viewportTranslation);
        Matrix4f.translate(xAxis3, oldViewportTranslation, viewportTranslation);
        Matrix4f.translate(yAxis3, viewportTranslation, viewportTranslation);
        perspective.calculateOriginData();
    }

    /**
     * Triggered actions on {@code SWT.MouseUp}
     *
     * @param event
     *            Event data.
     */
    // MARK MouseUp
    public void mouseUp(Event event) {

        syncManipulator();
        c3d.getManipulator().unlock();

        final DatFile datfile = c3d.getLockableDatFileReference();
        final VertexManager vm = datfile.getVertexManager();
        vm.addSnapshot();
        if (!datfile.isDrawSelection()) return;

        Vector2f oldPos = c3d.getOldMousePosition();
        c3d.setWarpedSelection(oldPos.x - event.x < 0 ^ oldPos.y - event.y < 0);

        mouseButtonPressed = 0;

        switch (event.button) {
        case MouseButton.LEFT:
            c3d.setDoingSelection(false);
            if (AddToolItem.isAddingSomething() && !datfile.isReadOnly()) {
                final boolean createVertex = vm.selectVertices(c3d, true, Math.max(Math.abs(oldPos.x - event.x), Math.abs(oldPos.y - event.y)) < 10);
                if (AddToolItem.isAddingLines() || AddToolItem.isAddingDistance()) {
                    Vertex vl1 = datfile.getObjVertex1();
                    if (vm.getSelectedVertices().size() == 1) {
                        if (vl1 != null) {
                            final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                            Vertex nv = vi.next();
                            datfile.setObjVertex1(vl1);
                            datfile.setObjVertex2(nv);
                            if (AddToolItem.isAddingLines()) {
                                vm.addLine(datfile.getNearestObjVertex1(), nv);
                            } else {
                                vm.addDistance(datfile.getNearestObjVertex1(), nv);
                            }
                            vm.getSelectedVertices().clear();
                            vm.setModified(true, true);
                        } else {
                            final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                            Vertex v1 = vi.next();
                            datfile.setObjVertex1(v1);
                            datfile.setObjVertex2(v1);
                            if (v1 != null) vm.getSelectedVertices().add(v1);
                        }
                        return;
                    }
                    if (vm.getSelectedVertices().isEmpty() && createVertex) {
                        if (vl1 == null) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex1(nv);
                            datfile.setObjVertex2(nv);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex1(vl1);
                            datfile.setObjVertex2(nv);
                            if (AddToolItem.isAddingLines()) {
                                vm.addLine(datfile.getNearestObjVertex1(), nv);
                            } else {
                                vm.addDistance(datfile.getNearestObjVertex1(), nv);
                            }
                            vm.getSelectedVertices().clear();
                            vm.setModified(true, true);
                        }
                    }
                    if (vm.getSelectedVertices().size() > 2) {
                        final Vertex vl2 = datfile.getObjVertex2();
                        if (vl1 != null) vm.getSelectedVertices().remove(vl1);
                        if (vl2 != null) vm.getSelectedVertices().remove(vl2);
                    }
                    if (vm.getSelectedVertices().size() == 2) {
                        final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                        Vertex v1 = vi.next();
                        Vertex v2 = vi.next();
                        if (AddToolItem.isAddingLines()) {
                            vm.addLine(v1, v2);
                        } else {
                            vm.addDistance(v1, v2);
                        }
                        vm.getSelectedVertices().clear();
                        vm.setModified(true, true);
                    }
                } else if (AddToolItem.isAddingTriangles() || AddToolItem.isAddingProtractor()) {
                    Vertex vt1 = datfile.getObjVertex1();
                    Vertex vt2 = datfile.getObjVertex2();
                    Vertex vt3 = datfile.getObjVertex3();
                    Vertex vn1 = datfile.getNearestObjVertex1();
                    Vertex vn2 = datfile.getNearestObjVertex2();
                    if (vt3 == null) {
                        if (vm.getSelectedVertices().size() == 2) {
                            if (vt2 != null) {
                                if (createVertex) {
                                    final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                                    final Vertex nv = new Vertex(cu3d);
                                    final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                                    Vertex v1 = vi.next();
                                    Vertex v2 = vi.next();
                                    datfile.setObjVertex3(nv);
                                    if (AddToolItem.isAddingTriangles()) {
                                        vm.addTriangle(v1, v2, nv, c3d);
                                    } else {
                                        vm.addProtractor(v1, nv, v2, c3d);
                                    }
                                    vm.getSelectedVertices().clear();
                                }
                            } else {
                                final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                                Vertex v1 = vi.next();
                                Vertex v2 = vi.next();
                                datfile.setObjVertex1(v1);
                                datfile.setObjVertex2(v2);
                                datfile.setObjVertex3(null);
                                if (v1 != null) vm.getSelectedVertices().add(v1);
                                if (v2 != null) vm.getSelectedVertices().add(v2);
                                datfile.setNearestObjVertex1(v1);
                                datfile.setNearestObjVertex2(v2);
                            }
                            return;
                        }
                        if (vm.getSelectedVertices().size() == 1) {
                            if (vt1 != null) {
                                if (vt2 != null) {
                                    final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                                    Vertex v1 = vi.next();
                                    datfile.setObjVertex3(v1);
                                    if (AddToolItem.isAddingTriangles()) {
                                        vm.addTriangle(vt1, vt2, v1, c3d);
                                    } else {
                                        vm.addProtractor(vt1, vt2, v1, c3d);
                                    }
                                    vm.getSelectedVertices().clear();
                                    return;
                                }
                                final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                                Vertex v1 = vi.next();
                                datfile.setObjVertex2(v1);
                                datfile.setObjVertex3(null);
                                if (v1 != null) vm.getSelectedVertices().add(v1);
                            } else {
                                final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                                Vertex v1 = vi.next();
                                datfile.setObjVertex1(v1);
                                datfile.setObjVertex2(null);
                                datfile.setObjVertex3(null);
                                if (v1 != null) vm.getSelectedVertices().add(v1);
                            }
                            return;
                        }
                        if (vm.getSelectedVertices().isEmpty() && createVertex) {
                            if (vt1 == null) {
                                final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                                final Vertex nv = new Vertex(cu3d);
                                datfile.setObjVertex1(nv);
                                datfile.setObjVertex2(null);
                                datfile.setObjVertex3(null);
                                if (nv != null) vm.getSelectedVertices().add(nv);
                            } else if (vt2 == null) {
                                final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                                final Vertex nv = new Vertex(cu3d);
                                datfile.setObjVertex2(nv);
                                datfile.setObjVertex3(null);
                                if (vt1 != null) vm.getSelectedVertices().add(vt1);
                                if (nv != null) vm.getSelectedVertices().add(nv);
                            } else {
                                final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                                final Vertex nv = new Vertex(cu3d);
                                datfile.setObjVertex3(nv);
                                if (AddToolItem.isAddingTriangles()) {
                                    vm.addTriangle(vt1, vt2, nv, c3d);
                                } else {
                                    vm.addProtractor(vt1, vt2, nv, c3d);
                                }
                                vm.getSelectedVertices().clear();
                                return;
                            }
                        }
                        if (vm.getSelectedVertices().size() > 3) {
                            if (vt1 != null) vm.getSelectedVertices().remove(vt1);
                            if (vt2 != null) vm.getSelectedVertices().remove(vt2);
                        }
                    } else {
                        if (vm.getSelectedVertices().isEmpty() && createVertex) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            if (vn1 != null && vn2 != null) {
                                datfile.setObjVertex1(nv);
                                datfile.setObjVertex2(vn1);
                                datfile.setObjVertex3(vn2);
                                if (AddToolItem.isAddingTriangles()) {
                                    vm.addTriangle(vn1, vn2, nv, c3d);
                                } else {
                                    vm.addProtractor(vn1, vn2, nv, c3d);
                                }
                                vm.getSelectedVertices().clear();
                                return;
                            }
                        } else if (vm.getSelectedVertices().size() == 1) {
                            final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                            Vertex v1 = vi.next();
                            if (v1 != null && vn1 != null && vn2 != null) {
                                datfile.setObjVertex1(v1);
                                datfile.setObjVertex2(vn1);
                                datfile.setObjVertex3(vn2);
                                if (AddToolItem.isAddingTriangles()) {
                                    vm.addTriangle(vn1, vn2, v1, c3d);
                                } else {
                                    vm.addProtractor(vn1, vn2, v1, c3d);
                                }
                                vm.getSelectedVertices().clear();
                                return;
                            }
                        }
                    }
                    if (vm.getSelectedVertices().size() == 3) {
                        final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                        Vertex v1 = vi.next();
                        Vertex v2 = vi.next();
                        Vertex v3 = vi.next();
                        datfile.setObjVertex1(v1);
                        datfile.setObjVertex2(v2);
                        datfile.setObjVertex3(v3);
                        if (AddToolItem.isAddingTriangles()) {
                            vm.addTriangle(v1, v2, v3, c3d);
                        } else {
                            vm.addProtractor(v1, v2, v3, c3d);
                        }
                        vm.getSelectedVertices().clear();
                    }
                } else if (AddToolItem.isAddingQuads()) {
                    Vertex vq1 = datfile.getObjVertex1();
                    Vertex vq2 = datfile.getObjVertex2();
                    Vertex vq3 = datfile.getObjVertex3();
                    Vertex vq4 = datfile.getObjVertex4();
                    Vertex vn1 = datfile.getNearestObjVertex1();
                    Vertex vn2 = datfile.getNearestObjVertex2();
                    if (vm.getSelectedVertices().isEmpty() && createVertex) {
                        if (vq1 == null) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex1(nv);
                            datfile.setObjVertex2(null);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vq2 == null) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex2(nv);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (vq1 != null) vm.getSelectedVertices().add(vq1);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vq3 == null || vq4 != null) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex3(nv);
                            datfile.setObjVertex4(null);
                            datfile.setObjVertex1(vn1);
                            datfile.setObjVertex2(vn2);
                            if (vn1 != null) vm.getSelectedVertices().add(vn1);
                            if (vn2 != null) vm.getSelectedVertices().add(vn2);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vq4 == null) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex4(nv);
                            vm.addQuad(vq1, vq2, vq3, nv, c3d);
                            vm.getSelectedVertices().clear();
                            return;
                        }
                    } else if (vm.getSelectedVertices().size() == 1) {
                        final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                        Vertex nv = vi.next();
                        if (vq1 == null) {
                            datfile.setObjVertex1(nv);
                            datfile.setObjVertex2(null);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vq2 == null) {
                            datfile.setObjVertex2(nv);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (vq1 != null) vm.getSelectedVertices().add(vq1);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vq3 == null || vq4 != null) {
                            datfile.setObjVertex3(nv);
                            datfile.setObjVertex4(null);
                            datfile.setObjVertex1(vn1);
                            datfile.setObjVertex2(vn2);
                            if (vn1 != null) vm.getSelectedVertices().add(vn1);
                            if (vn2 != null) vm.getSelectedVertices().add(vn2);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vq4 == null) {
                            datfile.setObjVertex4(nv);
                            vm.addQuad(vq1, vq2, vq3, nv, c3d);
                            vm.getSelectedVertices().clear();
                            return;
                        }
                    } else if (vm.getSelectedVertices().size() == 2) {
                        final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                        Vertex nv = vi.next();
                        Vertex nv2 = vi.next();
                        if (vq1 == null && vq2 == null) {
                            datfile.setObjVertex1(nv);
                            datfile.setObjVertex2(nv2);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                            if (nv2 != null) vm.getSelectedVertices().add(nv2);
                        } else if (vq3 == null && vq4 == null) {
                            datfile.setObjVertex3(nv);
                            datfile.setObjVertex4(nv2);
                            vm.addQuad(vq1, vq2, nv, nv2, c3d);
                            vm.getSelectedVertices().clear();
                            return;
                        } else if (vq1 != null && vq2 != null && vq3 != null && vq4 != null) {
                            datfile.setObjVertex1(vn1);
                            datfile.setObjVertex2(vn2);
                            datfile.setObjVertex3(nv);
                            datfile.setObjVertex4(nv2);
                            vm.addQuad(vn1, vn2, nv, nv2, c3d);
                            vm.getSelectedVertices().clear();
                            return;
                        } else if (vq1 != null && vq2 == null && vq3 == null && vq4 == null) {
                            // Add nothing.
                        }
                    } else if (vm.getSelectedVertices().size() == 3) {
                        final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                        Vertex nv = vi.next();
                        Vertex nv2 = vi.next();
                        Vertex nv3 = vi.next();
                        if (vq1 != null && vq2 == null && vq3 == null && vq4 == null) {
                            datfile.setObjVertex2(nv);
                            datfile.setObjVertex3(nv2);
                            datfile.setObjVertex4(nv3);
                            vm.addQuad(vq1, nv, nv2, nv3, c3d);
                            vm.getSelectedVertices().clear();
                            return;
                        } else if (vq1 == null && vq2 == null && vq3 == null && vq4 == null) {
                            datfile.setNearestObjVertex1(nv);
                            datfile.setNearestObjVertex2(nv2);
                            datfile.setObjVertex1(nv);
                            datfile.setObjVertex2(nv2);
                            datfile.setObjVertex3(nv3);
                            datfile.setObjVertex4(null);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                            if (nv2 != null) vm.getSelectedVertices().add(nv2);
                            if (nv3 != null) vm.getSelectedVertices().add(nv3);
                        }
                    }
                    if (vm.getSelectedVertices().size() > 4) {
                        if (vq1 != null) vm.getSelectedVertices().remove(vq1);
                        if (vq2 != null) vm.getSelectedVertices().remove(vq2);
                        if (vq3 != null) vm.getSelectedVertices().remove(vq3);
                        if (vq4 != null) vm.getSelectedVertices().remove(vq4);
                    }
                    if (vm.getSelectedVertices().size() == 4) {
                        final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                        Vertex v1 = vi.next();
                        Vertex v2 = vi.next();
                        Vertex v3 = vi.next();
                        Vertex v4 = vi.next();
                        datfile.setObjVertex1(v1);
                        datfile.setObjVertex2(v2);
                        datfile.setObjVertex3(v3);
                        datfile.setObjVertex4(v4);
                        vm.addQuad(v1, v2, v3, v4, c3d);
                        vm.getSelectedVertices().clear();
                    }
                } else if (AddToolItem.isAddingCondlines()) {
                    Vertex vc1 = datfile.getObjVertex1();
                    Vertex vc2 = datfile.getObjVertex2();
                    Vertex vc3 = datfile.getObjVertex3();
                    Vertex vc4 = datfile.getObjVertex4();
                    Vertex vn1 = datfile.getNearestObjVertex1();
                    Vertex vn2 = datfile.getNearestObjVertex2();
                    if (vm.getSelectedVertices().isEmpty() && createVertex) {
                        if (vc1 == null) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex1(nv);
                            datfile.setObjVertex2(null);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vc2 == null) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex2(nv);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (vc1 != null) vm.getSelectedVertices().add(vc1);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vc3 == null) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex3(nv);
                            datfile.setObjVertex4(null);
                            datfile.setObjVertex1(vn1);
                            datfile.setObjVertex2(vn2);
                            if (vn1 != null) vm.getSelectedVertices().add(vn1);
                            if (vn2 != null) vm.getSelectedVertices().add(vn2);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vc4 == null) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex4(nv);
                            vm.addCondline(vc1, vc2, vc3, nv);
                            vm.getSelectedVertices().clear();
                            vm.setModified(true, true);
                            return;
                        } else if (vc4 != null) {
                            final BigDecimal[] cu3d = c3d.getCursorSnapped3Dprecise();
                            final Vertex nv = new Vertex(cu3d);
                            datfile.setObjVertex1(vn1);
                            datfile.setObjVertex2(nv);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (vn1 != null) vm.getSelectedVertices().add(vn1);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        }
                    } else if (vm.getSelectedVertices().size() == 1) {
                        final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                        Vertex nv = vi.next();
                        if (vc1 == null) {
                            datfile.setObjVertex1(nv);
                            datfile.setObjVertex2(null);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vc2 == null) {
                            datfile.setObjVertex2(nv);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (vc1 != null) vm.getSelectedVertices().add(vc1);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vc3 == null) {
                            datfile.setObjVertex3(nv);
                            datfile.setObjVertex4(null);
                            datfile.setObjVertex1(vn1);
                            datfile.setObjVertex2(vn2);
                            if (vn1 != null) vm.getSelectedVertices().add(vn1);
                            if (vn2 != null) vm.getSelectedVertices().add(vn2);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        } else if (vc4 == null) {
                            datfile.setObjVertex4(nv);
                            vm.addCondline(vc1, vc2, vc3, nv);
                            vm.getSelectedVertices().clear();
                            vm.setModified(true, true);
                            return;
                        } else if (vc4 != null) {
                            datfile.setObjVertex1(vn1);
                            datfile.setObjVertex2(nv);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (vn1 != null) vm.getSelectedVertices().add(vn1);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                        }
                    } else if (vm.getSelectedVertices().size() == 2) {
                        final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                        Vertex nv = vi.next();
                        Vertex nv2 = vi.next();
                        if (vc1 == null && vc2 == null) {
                            datfile.setObjVertex1(nv);
                            datfile.setObjVertex2(nv2);
                            datfile.setObjVertex3(null);
                            datfile.setObjVertex4(null);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                            if (nv2 != null) vm.getSelectedVertices().add(nv2);
                        } else if (vc3 == null && vc4 == null) {
                            datfile.setObjVertex3(nv);
                            datfile.setObjVertex4(nv2);
                            vm.addCondline(vc1, vc2, nv, nv2);
                            vm.getSelectedVertices().clear();
                            vm.setModified(true, true);
                            return;
                        } else if (vc1 != null && vc2 != null && vc3 != null && vc4 != null) {
                            datfile.setObjVertex1(vn1);
                            datfile.setObjVertex2(vn2);
                            datfile.setObjVertex3(nv);
                            datfile.setObjVertex4(nv2);
                            vm.addCondline(vn1, vn2, nv, nv2);
                            vm.getSelectedVertices().clear();
                            vm.setModified(true, true);
                            return;
                        } else if (vc1 != null && vc2 == null && vc3 == null && vc4 == null) {
                            // Add nothing.
                        }
                    } else if (vm.getSelectedVertices().size() == 3) {
                        final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                        Vertex nv = vi.next();
                        Vertex nv2 = vi.next();
                        Vertex nv3 = vi.next();
                        if (vc1 != null && vc2 == null && vc3 == null && vc4 == null) {
                            datfile.setObjVertex2(nv);
                            datfile.setObjVertex3(nv2);
                            datfile.setObjVertex4(nv3);
                            vm.addCondline(vc1, nv, nv2, nv3);
                            vm.getSelectedVertices().clear();
                            vm.setModified(true, true);
                            return;
                        } else if (vc1 == null && vc2 == null && vc3 == null && vc4 == null) {
                            datfile.setNearestObjVertex1(nv);
                            datfile.setNearestObjVertex2(nv2);
                            datfile.setObjVertex1(nv);
                            datfile.setObjVertex2(nv2);
                            datfile.setObjVertex3(nv3);
                            datfile.setObjVertex4(null);
                            if (nv != null) vm.getSelectedVertices().add(nv);
                            if (nv2 != null) vm.getSelectedVertices().add(nv2);
                            if (nv3 != null) vm.getSelectedVertices().add(nv3);
                        }
                    }
                    if (vm.getSelectedVertices().size() > 4) {
                        if (vc1 != null) vm.getSelectedVertices().remove(vc1);
                        if (vc2 != null) vm.getSelectedVertices().remove(vc2);
                        if (vc3 != null) vm.getSelectedVertices().remove(vc3);
                        if (vc4 != null) vm.getSelectedVertices().remove(vc4);
                    }
                    if (vm.getSelectedVertices().size() == 4) {
                        final Iterator<Vertex> vi = vm.getSelectedVertices().iterator();
                        Vertex v1 = vi.next();
                        Vertex v2 = vi.next();
                        Vertex v3 = vi.next();
                        Vertex v4 = vi.next();
                        datfile.setObjVertex1(v1);
                        datfile.setObjVertex2(v2);
                        datfile.setObjVertex3(v3);
                        datfile.setObjVertex4(v4);
                        vm.addCondline(v1, v2, v3, v4);
                        vm.setModified(true, true);
                        vm.getSelectedVertices().clear();
                    }
                }
            } else if (translateAtSelect && !AddToolItem.isAddingSomething()) {
                Manipulator m = c3d.getManipulator();
                m.applyTranslationAtSelect(c3d);
                translateAtSelect = false;
                m.getXaxis().set(mX);
                m.getYaxis().set(mY);
                m.setAccurateXaxis(aXx, aXy, aXz);
                m.setAccurateYaxis(aYx, aYy, aYz);
                checkSyncEditMode(vm, datfile);
            } else if (TransformationModeToolItem.getWorkingAction() == WorkingMode.SELECT) {
                final SelectorSettings sels = MiscToolItem.loadSelectorSettings();
                switch (WorkingTypeToolItem.getWorkingType()) {
                case VERTICES:
                    if (!sels.isVertices()) break;
                    vm.selectVertices(c3d, false, false);
                    vm.reSelectSubFiles();
                    break;
                case LINES:
                    vm.selectLines(c3d, sels);
                    vm.reSelectSubFiles();
                    break;
                case FACES:
                    vm.selectFaces(c3d, event, sels);
                    vm.reSelectSubFiles();
                    break;
                case SUBFILES:
                    vm.selectSubfiles(c3d, event);
                    GDataCSG.selectCSG(c3d, event);
                    break;
                }
                checkSyncEditMode(vm, datfile);
                vm.syncWithTextEditors(true);
            } else if (!AddToolItem.isAddingSomething()) {
                c3d.getManipulator().applyTranslation(c3d);
                if (c3d.isQuicklyTransforming()) {
                    Editor3DWindow.getWindow().setWorkingLayer(ManipulatorAxisMode.NONE);
                    c3d.setQuicklyTransforming(false);
                }

                checkSyncEditMode(vm, datfile);
            }
            break;
        case MouseButton.RIGHT:
            vm.setSkipSyncWithTextEditor(false);
            KeyStateManager keyboard = c3d.getKeys();
            if (!keyboard.isCtrlPressed() && WorkbenchManager.getUserSettingState().isCancelAddViaMouse()) {
                datfile.setObjVertex1(null);
                datfile.setObjVertex2(null);
                datfile.setObjVertex3(null);
                datfile.setObjVertex4(null);
                datfile.setNearestObjVertex1(null);
                datfile.setNearestObjVertex2(null);
                if (AddToolItem.isAddingSomething()) vm.clearSelection();
            }
            if (c3d.isDoingSelection() || c3d.isQuicklyTransforming())
                break;
            float dx = event.x - oldMousePosition.x;
            float dy = oldMousePosition.y - event.y;
            if (Math.abs(dx) + Math.abs(dy) < 5.0) {

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new LDPartEditorException(ie);
                }

                java.awt.Point b = java.awt.MouseInfo.getPointerInfo().getLocation();
                final int x = (int) b.getX();
                final int y = (int) b.getY();

                Menu menu = c3d.getMenu();
                if (!menu.isDisposed()) {
                    menu.setLocation(x, y);
                    menu.setVisible(true);
                }
            }
            break;
        default:
            break;
        }

        if (!c3d.isQuicklyTransforming()) {
            c3d.getManipulator().resetTranslation();
        }

        syncManipulator();
    }

    public static void checkSyncEditMode(VertexManager vm, DatFile datfile) {
        Vertex v = null;
        if (vm.getSelectedVertices().size() == 1) {
            v = vm.getSelectedVertices().iterator().next();
        }
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            for (CTabItem t : w.getTabFolder().getItems()) {
                if (datfile.equals(((CompositeTab) t).getState().getFileNameObj())) {
                    CompositeTabState state = ((CompositeTab) t).getState();
                    if (v == null) {
                        if (state.isReplacingVertex()) {
                            state.setReplacingVertex(false);
                            vm.setVertexToReplace(null);
                            state.setToReplaceX(BigDecimal.ZERO);
                            state.setToReplaceY(BigDecimal.ZERO);
                            state.setToReplaceZ(BigDecimal.ZERO);
                        }
                    } else {
                        state.setReplacingVertex(!datfile.isReadOnly());
                        vm.setVertexToReplace(v);
                        state.setToReplaceX(v.xp);
                        state.setToReplaceY(v.yp);
                        state.setToReplaceZ(v.zp);
                    }
                }
            }
        }
    }

    public void syncManipulator() {
        if (c3d.isSyncManipulator() || !GDataCSG.getSelection(c3d.getLockableDatFileReference()).isEmpty()) {
            for (OpenGLRenderer renderer : Editor3DWindow.getRenders()) {
                Composite3D c3d2 = renderer.getC3D();
                if (!c3d2.isDisposed() && c3d != c3d2 && c3d.getLockableDatFileReference().equals(c3d2.getLockableDatFileReference())) {
                    c3d2.getManipulator().copyState(c3d.getManipulator());
                    ((ScalableComposite) c3d2.getParent()).redrawScales();
                    c3d2.getPerspectiveCalculator().initializeViewportPerspective();
                }
            }
        }
    }

    public void mouseDoubleClick(Event event) {
        c3d.setDoingSelection(false);
        NLogger.debug(MouseActions.class, "[Adjust rotation center]"); //$NON-NLS-1$

        VertexManager vm = c3d.getLockableDatFileReference().getVertexManager();

        vm.adjustRotationCenter(c3d, event);

        ViewIdleManager.refresh(c3d.getCanvas(), c3d.getRenderer());

        if (vm.getSelectedSubfiles().size() == 1) {
            final GData1 subfile = vm.getSelectedSubfiles().iterator().next();
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                Matrix4f m = subfile.getProductMatrix();
                Matrix mPrecise = subfile.getAccurateProductMatrix();
                MatrixOperations.moveManipulatorToSubfileOrCSGMatrix(c3d, mPrecise, m);
            }
        } else if (GDataCSG.getSelection(c3d.getLockableDatFileReference()).size() == 1) {
            GDataCSG csg = null;
            for (GDataCSG c1 : GDataCSG.getSelection(c3d.getLockableDatFileReference())) {
                csg = c1;
                break;
            }
            if (csg == null) {
                return;
            }
            Matrix4f m = csg.getLDrawMatrix();
            if (m == null) {
                return;
            }
            Matrix4f m2 = new Matrix4f(m);
            m2.m30 = m2.m30 / 1000f;
            m2.m31 = m2.m31 / 1000f;
            m2.m32 = m2.m32 / 1000f;
            Matrix mPrecise = new Matrix(m2);
            if (c3d.getLockableDatFileReference().equals(Project.getFileToEdit())) {
                MatrixOperations.moveManipulatorToSubfileOrCSGMatrix(c3d, mPrecise, m);
            }
        }
    }

    private void reMapMouseEvent(Event event) {
        final int mouseButtonLayout = WorkbenchManager.getUserSettingState().getMouseButtonLayout();
        if (mouseButtonLayout == MOUSE_LAYOUT_SWITCH_ROTATE_AND_TRANSLATE) {
            if (event.button == MouseButton.MIDDLE) {
                event.button = MouseButton.RIGHT;
            } else if (event.button == MouseButton.RIGHT) {
                event.button = MouseButton.MIDDLE;
            }
        }
    }
}