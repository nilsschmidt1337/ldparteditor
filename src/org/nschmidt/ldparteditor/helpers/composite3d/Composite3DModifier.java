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

import java.io.File;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.vector.Matrix4f;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.composites.CompositeContainer;
import org.nschmidt.ldparteditor.composites.CompositeScale;
import org.nschmidt.ldparteditor.composites.ScalableComposite;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.enums.OpenInWhat;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;

/**
 * Provides functions to perform view actions for the {@linkplain Composite3D}
 * (e.g. closing, h/v-splitting, rotating)
 *
 * @author nils
 *
 */
public class Composite3DModifier {

    // Non-public properties
    /** The 3D Composite [NOT PUBLIC YET] */
    private final Composite3D c3d;

    public Composite3DModifier(Composite3D c3d) {
        this.c3d = c3d;
    }

    /**
     * Displays the origin within {@linkplain Composite3D}.
     *
     * @param shown
     *            True if the origin should be shown
     */
    public void showOrigin(boolean shown) {
        NLogger.debug(Composite3DModifier.class, "[Show origin]"); //$NON-NLS-1$
        c3d.setOriginShown(shown);
    }

    /**
     * Displays the grid within {@linkplain Composite3D}.
     *
     * @param shown
     *            True if the origin should be shown
     */
    public void showGrid(boolean shown) {
        NLogger.debug(Composite3DModifier.class, "[Show grid]"); //$NON-NLS-1$
        c3d.setGridShown(shown);
    }

    /**
     * Switches light within {@linkplain Composite3D}.
     *
     * @param lightIsOn
     *            True if the light is on
     */
    public void switchLigths(boolean lightIsOn) {
        NLogger.debug(Composite3DModifier.class, "[Switch lights]"); //$NON-NLS-1$
        c3d.setLightOn(lightIsOn);
    }

    /**
     * Switches mesh lines within {@linkplain Composite3D}.
     *
     * @param lineDrawn
     *            True if mesh lines are drawn
     */
    public void switchMeshLines(boolean lineDrawn) {
        NLogger.debug(Composite3DModifier.class, "[Mesh lines]"); //$NON-NLS-1$
        c3d.setMeshLines(lineDrawn);
    }

    public void switchSubMeshLines(boolean lineDrawn) {
        NLogger.debug(Composite3DModifier.class, "[Subfile Mesh lines]"); //$NON-NLS-1$
        c3d.setSubMeshLines(lineDrawn);
    }

    public void setRenderMode(int renderMode) {
        c3d.setRenderMode(renderMode);
    }

    public void setLineMode(int lineMode) {
        c3d.setLineMode(lineMode);
    }

    public void switchLockedDat(boolean locked) {
        c3d.setDatFileLockedOnDisplay(locked);
    }

    public void switchAnaglyph3D(boolean anaglyph) {
        c3d.setAnaglyph3d(anaglyph);
    }

    public void switchShowingLogo(boolean logo) {
        c3d.setShowingLogo(logo);
    }

    /**
     * Switches mesh lines within {@linkplain Composite3D}.
     *
     * @param verticesShown
     *            True if (visible) vertices are drawn
     */
    public void switchVertices(boolean verticesShown) {
        NLogger.debug(Composite3DModifier.class, "[Visible Vertices]"); //$NON-NLS-1$
        c3d.setShowingVertices(verticesShown);
    }

    /**
     * Switches mesh lines within {@linkplain Composite3D}.
     *
     * @param verticesShown
     *            True if hidden vertices are drawn
     */
    public void switchHiddenVertices(boolean verticesShown) {
        NLogger.debug(Composite3DModifier.class, "[All Vertices]"); //$NON-NLS-1$
        c3d.setShowingHiddenVertices(verticesShown);
    }

    /**
     * Displays a horizontal and vertical scale around the
     * {@linkplain Composite3D}.
     *
     * @param shown
     *            True if the scale should be shown
     */
    public void showScale(boolean shown) {
        NLogger.debug(Composite3DModifier.class, "[Show scale]"); //$NON-NLS-1$
        if (shown) {
            new CompositeScale(c3d.getCompositeContainer(), c3d, SWT.NONE);
        } else {
            Composite oldScale = c3d.getParent();
            c3d.setParent(c3d.getCompositeContainer());
            oldScale.dispose();
        }
        c3d.getCompositeContainer().layout();
    }

    /**
     * Splits this composite into two by creating a new
     * {@link org.eclipse.swt.custom.SashForm}, a new
     * {@link org.nschmidt.ldparteditor.composites.Composite3D} and including
     * the old composite in the north of the vertical SashForm.
     */
    public SashForm splitViewHorizontally() {
        NLogger.debug(Composite3DModifier.class, "[Split horizontally]"); //$NON-NLS-1$

        int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
        int[] superSashWeights = c3d.getSashForm().getWeights();

        boolean isUpperComposite;
        try {
            isUpperComposite = ((ScalableComposite) c3d.getSashForm().getChildren()[0]).getComposite3D().equals(c3d);
        } catch (ClassCastException e) {
            isUpperComposite = false;
        }

        final SashForm newParentSashForm = new SashForm(c3d.getSashForm(), SWT.VERTICAL);
        c3d.getCompositeContainer().setParent(newParentSashForm);

        CompositeContainer southComposite = new CompositeContainer(newParentSashForm, false);
        newParentSashForm.setWeights(new int[] { 1, 1 });
        southComposite.moveBelow(c3d.getCompositeContainer());

        if (isSashFormChild(newParentSashForm.getParent())) {
            if (isUpperComposite) {
                newParentSashForm.moveAbove(newParentSashForm.getParent().getChildren()[0]);
            } else {
                newParentSashForm.moveBelow(newParentSashForm.getParent().getChildren()[0]);
            }
        } else {
            newParentSashForm.moveBelow(newParentSashForm.getParent().getChildren()[0]);
        }
        newParentSashForm.getParent().layout();
        ((SashForm) newParentSashForm.getParent()).setWeights(superSashWeights);
        Editor3DWindow.getSashForm().setWeights(mainSashWeights);
        return newParentSashForm;
    }

    /**
     * Splits this composite into two by creating a new
     * {@link org.eclipse.swt.custom.SashForm}, a new
     * {@link org.nschmidt.ldparteditor.composites.Composite3D} and including
     * the old composite in the west of the horizontal SashForm.
     */
    public SashForm splitViewVertically() {
        NLogger.debug(Composite3DModifier.class, "[Split vertically]"); //$NON-NLS-1$

        int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
        int[] superSashWeights = c3d.getSashForm().getWeights();

        boolean isUpperComposite;
        try {
            isUpperComposite = ((ScalableComposite) c3d.getSashForm().getChildren()[0]).getComposite3D().equals(c3d);
        } catch (ClassCastException e) {
            isUpperComposite = false;
        }

        final SashForm newParentSashForm = new SashForm(c3d.getSashForm(), SWT.HORIZONTAL);
        c3d.getCompositeContainer().setParent(newParentSashForm);

        CompositeContainer southComposite = new CompositeContainer(newParentSashForm, false);
        newParentSashForm.setWeights(new int[] { 1, 1 });
        southComposite.moveBelow(c3d.getCompositeContainer());

        if (isSashFormChild(newParentSashForm.getParent())) {
            if (isUpperComposite) {
                newParentSashForm.moveAbove(newParentSashForm.getParent().getChildren()[0]);
            } else {
                newParentSashForm.moveBelow(newParentSashForm.getParent().getChildren()[0]);
            }
        } else {
            newParentSashForm.moveBelow(newParentSashForm.getParent().getChildren()[0]);
        }
        newParentSashForm.getParent().layout();
        ((SashForm) newParentSashForm.getParent()).setWeights(superSashWeights);
        Editor3DWindow.getSashForm().setWeights(mainSashWeights);
        return newParentSashForm;
    }

    /**
     * Spins the {@link SashForm} content clockwise
     */
    public void spinView() {
        NLogger.debug(Composite3DModifier.class, "[Spin view]"); //$NON-NLS-1$
        // Spin only, if it is not the last opened view
        if (isSashFormChild(c3d.getSashForm())) {
            int newStyle = SWT.NONE;
            if ((c3d.getSashForm().getStyle() & SWT.HORIZONTAL) != 0) {
                newStyle = SWT.VERTICAL;
            } else {
                newStyle = SWT.HORIZONTAL;
            }
            int[] sashWeights = c3d.getSashForm().getWeights();
            int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
            int[] superSashWeights = ((SashForm) c3d.getSashForm().getParent()).getWeights();
            Composite oldParentSashForm = c3d.getSashForm();
            Composite oldGrandpaSashForm = oldParentSashForm.getParent();
            boolean isUpperComposite = oldParentSashForm.getChildren()[0].equals(c3d.getCompositeContainer());

            if (isUpperComposite) {
                if (newStyle == SWT.HORIZONTAL) {
                    reverseChilds(c3d.getSashForm());
                }
            } else {
                if (newStyle == SWT.HORIZONTAL) {
                    reverseChilds(c3d.getSashForm());
                }
            }

            oldParentSashForm.setOrientation(newStyle);
            oldGrandpaSashForm.layout();
            ((SashForm) oldParentSashForm).setWeights(sashWeights);
            ((SashForm) oldGrandpaSashForm).setWeights(superSashWeights);
            Editor3DWindow.getSashForm().setWeights(mainSashWeights);
        }
    }

    /**
     * Disposes this composite if the user wants to close the perspective
     */
    public void closeView() {
        NLogger.debug(Composite3DModifier.class, "[Close view]"); //$NON-NLS-1$
        // Close only, if it is not the last opened view
        if (isSashFormChild(c3d.getSashForm())) {

            int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();
            int[] superSashWeights = ((SashForm) c3d.getSashForm().getParent()).getWeights();

            Composite oldParentSashForm = c3d.getSashForm();
            Composite oldGrandpaSashForm = oldParentSashForm.getParent();
            boolean isUpperComposite = oldGrandpaSashForm.getChildren()[0].equals(c3d.getSashForm());

            Control otherControl;
            if (!(oldParentSashForm.getChildren()[0] instanceof SashForm) && ((ScalableComposite) oldParentSashForm.getChildren()[0]).getComposite3D().equals(c3d)) {
                otherControl = oldParentSashForm.getChildren()[1];
            } else {
                otherControl = oldParentSashForm.getChildren()[0];
            }
            otherControl.setParent(oldGrandpaSashForm);

            if (isSashFormChild(oldParentSashForm)) {
                if (isUpperComposite) {
                    otherControl.moveAbove(oldGrandpaSashForm.getChildren()[0]);
                } else {
                    otherControl.moveBelow(oldGrandpaSashForm.getChildren()[0]);
                }
            }

            GLCanvas canvas = c3d.getCanvas();
            OpenGLRenderer renderer = c3d.getRenderer();
            if (!canvas.isCurrent()) {
                canvas.setCurrent();
                try {
                    GLContext.useContext(canvas);
                } catch (LWJGLException e) {
                    NLogger.error(OpenGLRenderer.class, e);
                }
            }
            renderer.dispose();

            oldParentSashForm.dispose();

            oldGrandpaSashForm.layout();
            ((SashForm) oldGrandpaSashForm).setWeights(superSashWeights);
            Editor3DWindow.getSashForm().setWeights(mainSashWeights);
            c3d.dispose();
        } else {
            int[] mainSashWeights = Editor3DWindow.getSashForm().getWeights();

            Composite oldParentSashForm = c3d.getSashForm();

            GLCanvas canvas = c3d.getCanvas();
            OpenGLRenderer renderer = c3d.getRenderer();
            if (!canvas.isCurrent()) {
                canvas.setCurrent();
                try {
                    GLContext.useContext(canvas);
                } catch (LWJGLException e) {
                    NLogger.error(OpenGLRenderer.class, e);
                }
            }
            renderer.dispose();

            c3d.dispose();

            oldParentSashForm.getChildren()[1].dispose();

            Composite nc = new Composite(oldParentSashForm, SWT.NONE);
            nc.setData("%EMPTY%"); //$NON-NLS-1$
            nc.moveBelow(oldParentSashForm.getChildren()[0]);

            DropTarget dt = new DropTarget(nc, DND.DROP_DEFAULT | DND.DROP_MOVE );
            dt.setTransfer(new Transfer[] { FileTransfer.getInstance() });
            dt.addDropListener(new DropTargetAdapter() {
                public void drop(DropTargetEvent event) {
                    String fileList[] = null;
                    FileTransfer ft = FileTransfer.getInstance();
                    if (ft.isSupportedType(event.currentDataType)) {
                        fileList = (String[]) event.data;
                        if (fileList != null) {
                            for (String f : fileList) {
                                NLogger.debug(getClass(), f);
                                if (f.toLowerCase(Locale.ENGLISH).endsWith(".dat")) { //$NON-NLS-1$
                                    final File fileToOpen = new File(f);
                                    if (!fileToOpen.exists() || fileToOpen.isDirectory()) continue;
                                    DatFile df = Editor3DWindow.getWindow().openDatFile(Editor3DWindow.getWindow().getShell(), OpenInWhat.EDITOR_3D, f);
                                    if (df != null) {
                                        Editor3DWindow.getWindow().addRecentFile(df);
                                        final File f2 = new File(df.getNewName());
                                        if (f2.getParentFile() != null) {
                                            Project.setLastVisitedPath(f2.getParentFile().getAbsolutePath());
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            });

            oldParentSashForm.layout();
            Editor3DWindow.getSashForm().setWeights(mainSashWeights);
            GuiManager.updateStatus();
            Project.setFileToEdit(null);
        }
    }

    /**
     * Reverses the child order of SashForms
     *
     * @param cmp
     *            the {@link SashForm} to process
     */
    private void reverseChilds(SashForm sashForm) {
        sashForm.getChildren()[1].moveAbove(sashForm.getChildren()[0]);
    }

    private boolean isSashFormChild(Composite sashFormToCheck) {
        return sashFormToCheck.getToolTipText() == null;
    }

    /**
     * Moves the adjacent Sash auto-magically
     */
    public void moveSashOnResize() {
        if (isSashFormChild(c3d.getSashForm()) && c3d.getSashForm().getParent() instanceof SashForm && isSashFormChild(c3d.getSashForm().getParent())) {
            Composite symmetricalSashAxis = c3d.getSashForm().getParent();
            if (symmetricalSashAxis.getChildren()[0] instanceof SashForm && symmetricalSashAxis.getChildren()[1] instanceof SashForm) {
                if (((SashForm) symmetricalSashAxis).getStyle() != ((SashForm) symmetricalSashAxis.getChildren()[0]).getStyle()
                        && ((SashForm) symmetricalSashAxis).getStyle() != ((SashForm) symmetricalSashAxis.getChildren()[1]).getStyle()) {
                    if (symmetricalSashAxis.getChildren()[0].equals(c3d.getSashForm())) {
                        ((SashForm) symmetricalSashAxis.getChildren()[1]).setWeights(c3d.getSashForm().getWeights());
                    } else {
                        ((SashForm) symmetricalSashAxis.getChildren()[0]).setWeights(c3d.getSashForm().getWeights());
                    }
                }
            }
        }
    }

    public void switchAxis(boolean selection) {
        c3d.setShowingAxis(selection);
    }

    public void switchLabel(boolean selection) {
        c3d.setShowingLabels(selection);
    }

    public void zoomToFit() {
        final PerspectiveCalculator pc = c3d.getPerspectiveCalculator();
        float max_x = 0f;
        float max_y = 0f;
        for (Vertex v : c3d.getLockableDatFileReference().getVertexManager().getVertices()) {
            float ax = Math.abs(v.x);
            float ay = Math.abs(v.y);
            if (ax > max_x) max_x = ax;
            if (ay > max_y) max_y = ay;
        }
        Matrix4f id = new Matrix4f();
        Matrix4f.setIdentity(id);
        c3d.getTranslation().load(id);
        Rectangle b = c3d.getBounds();
        if (max_x > max_y) {
            c3d.setZoom(b.width / (max_x * 4f * View.PIXEL_PER_LDU));
        } else {
            c3d.setZoom(b.height / (max_y * 4f * View.PIXEL_PER_LDU));
        }
        pc.setZoom_exponent((float) (Math.log10(c3d.getZoom()) + 3f) * 10f);

        if (Float.isInfinite(c3d.getZoom()) || Float.isInfinite(pc.getZoom_exponent()) || Float.isNaN(c3d.getZoom()) || Float.isNaN(pc.getZoom_exponent())) {
            pc.setZoom_exponent(-20f);
            c3d.setZoom((float) Math.pow(10.0d, -20f / 10 - 3));
        }

        c3d.setViewportPixelPerLDU(c3d.getZoom() * View.PIXEL_PER_LDU);
        GuiManager.updateStatus(c3d);
        ((ScalableComposite) c3d.getParent()).redrawScales();
        pc.initializeViewportPerspective();
    }
}
