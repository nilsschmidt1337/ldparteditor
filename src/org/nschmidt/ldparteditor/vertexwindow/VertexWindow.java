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

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Set;

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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

    private long showupTime = System.currentTimeMillis();

    private final BigDecimalSpinner[] spn_X = new BigDecimalSpinner[1];
    private final BigDecimalSpinner[] spn_Y = new BigDecimalSpinner[1];
    private final BigDecimalSpinner[] spn_Z = new BigDecimalSpinner[1];

    private final NButton[] btn_Copy = new NButton[1];
    private final NButton[] btn_Paste = new NButton[1];
    private final NButton[] btn_Merge = new NButton[1];

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
                requestSelfDestruct();
            }

            @Override
            public void shellClosed(ShellEvent consumed) {}

            @Override
            public void shellActivated(ShellEvent consumed) {}
        });
        spn_X[0].addValueChangeListener(spn -> changeVertex());
        spn_Y[0].addValueChangeListener(spn -> changeVertex());
        spn_Z[0].addValueChangeListener(spn -> changeVertex());
        btn_Paste[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final Vertex clipboardVertex = VertexManager.getSingleVertexFromClipboard();
                if (clipboardVertex != null) {
                    updateVertex(clipboardVertex);
                    changeVertex();
                }
            }
        });
        btn_Copy[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                VertexManager.copySingleVertexIntoClipboard(selectedVertex);
            }
        });
        btn_Merge[0].addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final Composite3D lastHoveredC3d = DatFile.getLastHoveredComposite();
                if (lastHoveredC3d == null) return;
                final DatFile df = lastHoveredC3d.getLockableDatFileReference();
                final VertexManager vm = df.getVertexManager();
                vm.setXyzOrTranslateOrTransform(selectedVertex, null, TransformationMode.SET, true, true, true, true, true, ManipulatorScope.GLOBAL);
                vm.setVertexToReplace(selectedVertex);
                lastHoveredC3d.getMouse().checkSyncEditMode(vm, df);
            }
        });
    }

    private void changeVertex() {
        if (isUpdating) return;
        selectedVertex = new Vertex(spn_X[0].getValue(), spn_Y[0].getValue(), spn_Z[0].getValue());
        final Composite3D lastHoveredC3d = DatFile.getLastHoveredComposite();
        if (lastHoveredC3d == null) return;
        final DatFile df = lastHoveredC3d.getLockableDatFileReference();
        final VertexManager vm = df.getVertexManager();
        btn_Merge[0].setEnabled(vm.getVertices().contains(selectedVertex));
        if (!btn_Merge[0].isEnabled()) {
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
        if (lastHoveredC3d == null) return;

        final VertexWindow vertexWindow = Editor3DWindow.getWindow().getVertexWindow();
        final DatFile df = lastHoveredC3d.getLockableDatFileReference();
        final Set<Vertex> selectedVertices = df.getVertexManager().getSelectedVertices();
        final boolean singleVertex = !df.isReadOnly() && selectedVertices.size() == 1;
        Vertex newSelectedVertex = new Vertex(0,0,0);

        if (singleVertex) {
            try {
                newSelectedVertex = selectedVertices.iterator().next();
            } catch (NoSuchElementException consumed) {}
            vertexWindow.renew();
        }

        if (singleVertex && vertexWindow.getShell() == null) {
            vertexWindow.run();
            lastHoveredC3d.setFocus();
            Editor3DWindow.getWindow().getShell().setActive();
        } else if (!singleVertex && vertexWindow.getShell() != null) {
            vertexWindow.close();
        }

        if (singleVertex) {
            vertexWindow.updateVertex(newSelectedVertex);
        }

        final Shell vertexWindowShell = vertexWindow.getShell();
        if (vertexWindowShell == null || vertexWindowShell.isDisposed()) {
            return;
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
            spn_X[0].setValue(selectedVertex.X);
            spn_Y[0].setValue(selectedVertex.Y);
            spn_Z[0].setValue(selectedVertex.Z);
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
                Composite cmp_txt = new Composite(vertexWindow, SWT.NONE);
                cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmp_txt.setLayout(new GridLayout(5, true));

                Label lbl_vertexData = new Label(cmp_txt, SWT.NONE);
                lbl_vertexData.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                lbl_vertexData.setText("Vertex data:"); //$NON-NLS-1$ I18N Needs translation!

                {
                    NButton btn_Copy = new NButton(cmp_txt, Cocoa.getStyle());
                    this.btn_Copy[0] = btn_Copy;
                    btn_Copy.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
                    btn_Copy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
                    KeyStateManager.addTooltipText(btn_Copy, I18n.COPYNPASTE_Copy, Task.COPY);
                }
                {
                    NButton btn_Paste = new NButton(cmp_txt, Cocoa.getStyle());
                    this.btn_Paste[0] = btn_Paste;
                    btn_Paste.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
                    btn_Paste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
                    KeyStateManager.addTooltipText(btn_Paste, I18n.COPYNPASTE_Paste, Task.PASTE);
                }
            }

            {
                Composite cmp_txt = new Composite(vertexWindow, SWT.NONE);
                cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmp_txt.setLayout(new GridLayout(1, true));

                BigDecimalSpinner spn_X = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
                this.spn_X[0] = spn_X;
                spn_X.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                spn_X.setMaximum(new BigDecimal(1000000));
                spn_X.setMinimum(new BigDecimal(-1000000));
                spn_X.setValue(selectedVertex.X);
            }

            {
                Composite cmp_txt = new Composite(vertexWindow, SWT.NONE);
                cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmp_txt.setLayout(new GridLayout(1, true));

                BigDecimalSpinner spn_Y = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
                this.spn_Y[0] = spn_Y;
                spn_Y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                spn_Y.setMaximum(new BigDecimal(1000000));
                spn_Y.setMinimum(new BigDecimal(-1000000));
                spn_Y.setValue(selectedVertex.Y);
            }

            {
                Composite cmp_txt = new Composite(vertexWindow, SWT.NONE);
                cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmp_txt.setLayout(new GridLayout(1, true));

                BigDecimalSpinner spn_Z = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
                this.spn_Z[0] = spn_Z;
                spn_Z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                spn_Z.setMaximum(new BigDecimal(1000000));
                spn_Z.setMinimum(new BigDecimal(-1000000));
                spn_Z.setValue(selectedVertex.Z);
            }

            {
                Composite cmp_txt = new Composite(vertexWindow, SWT.NONE);
                cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmp_txt.setLayout(new GridLayout(1, true));

                NButton btn_Merge = new NButton(cmp_txt, Cocoa.getStyle());
                this.btn_Merge[0] = btn_Merge;
                btn_Merge.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                btn_Merge.setImage(ResourceManager.getImage("icon16_warning.png")); //$NON-NLS-1$
                btn_Merge.setText(I18n.E3D_MergeVertex);
                btn_Merge.setEnabled(false);
            }
        }

        vertexWindow.pack();

        return vertexWindow;
    }

    public void renew() {
        showupTime = System.currentTimeMillis();
    }

    public boolean isYoung() {
        return Math.abs(showupTime - System.currentTimeMillis()) < 200;
    }

    public void requestClose() {
        new Thread(() -> {
            try {
                Thread.sleep(200);
                Display.getDefault().asyncExec(() -> {
                    if (Editor3DWindow.getWindow().getShell().isFocusControl()) {
                        close();
                    }
                });
            } catch (InterruptedException consumed) {}
        }).start();
    }

    private void requestSelfDestruct() {
        new Thread(() -> {
            try {
                Thread.sleep(100);
                Display.getDefault().asyncExec(() -> {
                    if (!Editor3DWindow.getWindow().getShell().isFocusControl() && !isYoung()) {
                        close();
                    }
                });
            } catch (InterruptedException consumed) {}
        }).start();
    }
}
