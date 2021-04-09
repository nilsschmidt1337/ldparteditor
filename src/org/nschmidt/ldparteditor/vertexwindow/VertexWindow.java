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
package org.nschmidt.ldparteditor.vertexwindow;

import static org.nschmidt.ldparteditor.helpers.WidgetUtility.widgetUtil;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.Vertex;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enums.ManipulatorScope;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.TransformationMode;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.ShellHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * A window for manipulating the coordinates of a single vertex
 */
public class VertexWindow extends ApplicationWindow {

    private static Vertex selectedVertex = new Vertex(0,0,0);

    private final BigDecimalSpinner[] spnXPtr = new BigDecimalSpinner[1];
    private final BigDecimalSpinner[] spnYPtr = new BigDecimalSpinner[1];
    private final BigDecimalSpinner[] spnZPtr = new BigDecimalSpinner[1];

    private final NButton[] btnCopyPtr = new NButton[1];
    private final NButton[] btnPastePtr = new NButton[1];
    private final NButton[] btnMergePtr = new NButton[1];

    private boolean isUpdating = false;

    /**
     * Creates a new instance of the vertex window
     */
    public VertexWindow() {
        super(null);
    }

    /**
     * Brings a new instance of this vertex window to run
     */
    public void run() {
        this.setShellStyle(SWT.ON_TOP);
        this.setParentShell(Editor3DWindow.getWindow().getShell());
        this.create();
        this.open();
        // MARK Add listeners...
        getShell().addShellListener(new ShellListener() {
            @Override
            public void shellIconified(ShellEvent consumed) {}

            @Override
            public void shellDeiconified(ShellEvent consumed) { }

            @Override
            public void shellDeactivated(ShellEvent e) {
                Display.getDefault().timerExec(1000, () -> {
                    if (Display.getDefault().getActiveShell() == null) {
                        close();
                    }
                });
            }

            @Override
            public void shellClosed(ShellEvent consumed) {}

            @Override
            public void shellActivated(ShellEvent consumed) {}
        });
        spnXPtr[0].addValueChangeListener(spn -> changeVertex());
        spnYPtr[0].addValueChangeListener(spn -> changeVertex());
        spnZPtr[0].addValueChangeListener(spn -> changeVertex());
        widgetUtil(btnPastePtr[0]).addSelectionListener(e -> {
            final Vertex clipboardVertex = VertexManager.getSingleVertexFromClipboard();
            if (clipboardVertex != null) {
                updateVertex(clipboardVertex);
                changeVertex();
            }
        });
        widgetUtil(btnCopyPtr[0]).addSelectionListener(e ->
            VertexManager.copySingleVertexIntoClipboard(selectedVertex)
        );
        widgetUtil(btnMergePtr[0]).addSelectionListener(e -> {
            final Composite3D lastHoveredC3d = DatFile.getLastHoveredComposite();
            if (lastHoveredC3d == null) return;
            final DatFile df = lastHoveredC3d.getLockableDatFileReference();
            final VertexManager vm = df.getVertexManager();
            vm.setXyzOrTranslateOrTransform(selectedVertex, null, TransformationMode.SET, true, true, true, true, true, ManipulatorScope.GLOBAL);
            vm.setVertexToReplace(selectedVertex);
            lastHoveredC3d.getMouse().checkSyncEditMode(vm, df);
        });
    }

    private void changeVertex() {
        if (isUpdating) return;
        selectedVertex = new Vertex(spnXPtr[0].getValue(), spnYPtr[0].getValue(), spnZPtr[0].getValue());
        final Composite3D lastHoveredC3d = DatFile.getLastHoveredComposite();
        if (lastHoveredC3d == null) return;
        final DatFile df = lastHoveredC3d.getLockableDatFileReference();
        final VertexManager vm = df.getVertexManager();
        btnMergePtr[0].setEnabled(vm.getVertices().contains(selectedVertex));
        if (!btnMergePtr[0].isEnabled()) {
            vm.setXyzOrTranslateOrTransform(selectedVertex, null, TransformationMode.SET, true, true, true, true, true, ManipulatorScope.GLOBAL);
        }
        vm.setVertexToReplace(selectedVertex);
        lastHoveredC3d.getMouse().checkSyncEditMode(vm, df);
    }

    /**
     * Places the vertex window on the 3D editor
     */
    public static void placeVertexWindow() {
        final Composite3D lastHoveredC3d = DatFile.getLastHoveredComposite();
        if (lastHoveredC3d == null || Display.getDefault().getActiveShell() == null) return;

        final VertexWindow vertexWindow = Editor3DWindow.getWindow().getVertexWindow();
        final DatFile df = lastHoveredC3d.getLockableDatFileReference();
        final Set<Vertex> selectedVertices = df.getVertexManager().getSelectedVertices();
        final boolean singleVertexSelected = !df.isReadOnly() && selectedVertices.size() == 1;
        final boolean addingSomething = Editor3DWindow.getWindow().isAddingSomething();

        final boolean windowShouldBeDisplayed = singleVertexSelected && !addingSomething;

        Vertex newSelectedVertex = new Vertex(0,0,0);

        if (singleVertexSelected) {
            try {
                newSelectedVertex = selectedVertices.iterator().next();
            } catch (NoSuchElementException consumed) {}
        }

        if (windowShouldBeDisplayed && vertexWindow.getShell() == null) {
            vertexWindow.run();
            lastHoveredC3d.setFocus();
            Editor3DWindow.getWindow().getShell().setActive();
        } else if (!windowShouldBeDisplayed && vertexWindow.getShell() != null) {
            vertexWindow.close();
        }

        final Shell vertexWindowShell = vertexWindow.getShell();
        if (vertexWindowShell == null || vertexWindowShell.isDisposed()) {
            return;
        }

        if (singleVertexSelected) {
            vertexWindow.updateVertex(newSelectedVertex);
        }

        final Point old = vertexWindowShell.getLocation();
        final Point a = ShellHelper.absolutePositionOnShell(lastHoveredC3d);
        final Point s = vertexWindowShell.getSize();

        final int xPos = a.x - s.x + lastHoveredC3d.getSize().x;
        final int yPos = a.y;

        if (old.x != xPos || old.y != yPos) {
            vertexWindowShell.setLocation(xPos, yPos);
        }
    }

    private void updateVertex(Vertex selected) {
        if (!selectedVertex.equals(selected)) {
            selectedVertex = selected;
            isUpdating = true;
            spnXPtr[0].setValue(selectedVertex.xp);
            spnYPtr[0].setValue(selectedVertex.yp);
            spnZPtr[0].setValue(selectedVertex.zp);
            isUpdating = false;
        }
    }

    @Override
    protected Control createContents(Composite parent) {
        final Composite vertexWindow = new Composite(parent, SWT.NONE);
        final String NUMBER_FORMAT = View.NUMBER_FORMAT8F;

        GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = -2;
        gridLayout.horizontalSpacing = 1;
        vertexWindow.setLayout(gridLayout);

        {
            {
                Composite cmpTxt = new Composite(vertexWindow, SWT.NONE);
                cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmpTxt.setLayout(new GridLayout(5, true));

                Label lblVertexData = new Label(cmpTxt, SWT.NONE);
                lblVertexData.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                lblVertexData.setText("Vertex data:"); //$NON-NLS-1$ I18N Needs translation!

                {
                    NButton btnCopy = new NButton(cmpTxt, Cocoa.getStyle());
                    this.btnCopyPtr[0] = btnCopy;
                    btnCopy.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
                    btnCopy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
                    KeyStateManager.addTooltipText(btnCopy, I18n.COPYNPASTE_COPY, Task.COPY);
                }
                {
                    NButton btnPaste = new NButton(cmpTxt, Cocoa.getStyle());
                    this.btnPastePtr[0] = btnPaste;
                    btnPaste.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
                    btnPaste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
                    KeyStateManager.addTooltipText(btnPaste, I18n.COPYNPASTE_PASTE, Task.PASTE);
                }
            }

            {
                Composite cmpTxt = new Composite(vertexWindow, SWT.NONE);
                cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmpTxt.setLayout(new GridLayout(1, true));

                BigDecimalSpinner spnX = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT);
                this.spnXPtr[0] = spnX;
                spnX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                spnX.setMaximum(new BigDecimal(1000000));
                spnX.setMinimum(new BigDecimal(-1000000));
                spnX.setValue(selectedVertex.xp);
            }

            {
                Composite cmpTxt = new Composite(vertexWindow, SWT.NONE);
                cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmpTxt.setLayout(new GridLayout(1, true));

                BigDecimalSpinner spnY = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT);
                this.spnYPtr[0] = spnY;
                spnY.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                spnY.setMaximum(new BigDecimal(1000000));
                spnY.setMinimum(new BigDecimal(-1000000));
                spnY.setValue(selectedVertex.yp);
            }

            {
                Composite cmpTxt = new Composite(vertexWindow, SWT.NONE);
                cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmpTxt.setLayout(new GridLayout(1, true));

                BigDecimalSpinner spnZ = new BigDecimalSpinner(cmpTxt, SWT.NONE, NUMBER_FORMAT);
                this.spnZPtr[0] = spnZ;
                spnZ.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                spnZ.setMaximum(new BigDecimal(1000000));
                spnZ.setMinimum(new BigDecimal(-1000000));
                spnZ.setValue(selectedVertex.zp);
            }

            {
                Composite cmpTxt = new Composite(vertexWindow, SWT.NONE);
                cmpTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmpTxt.setLayout(new GridLayout(1, true));

                NButton btnMerge = new NButton(cmpTxt, Cocoa.getStyle());
                this.btnMergePtr[0] = btnMerge;
                btnMerge.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                btnMerge.setImage(ResourceManager.getImage("icon16_warning.png")); //$NON-NLS-1$
                btnMerge.setText(I18n.E3D_MERGE_VERTEX);
                btnMerge.setEnabled(false);
            }
        }

        vertexWindow.pack();

        return vertexWindow;
    }
}
