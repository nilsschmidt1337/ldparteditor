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

import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.nschmidt.ldparteditor.composites.Composite3D;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.helpers.ShellHelper;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.opengl.OpenGLRenderer;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * @author nils
 *
 */
public class VertexWindow extends ApplicationWindow {


    private long showupTime = System.currentTimeMillis();

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
    }

    /**
     * Places the vertex window on the 3D editor
     */
    public static void placeVertexWindow() {
        final VertexWindow vertexWindow = Editor3DWindow.getWindow().getVertexWindow();
        final Composite3D lastHoveredC3d = DatFile.getLastHoveredComposite();

        for (OpenGLRenderer renderer : Editor3DWindow.renders) {
            final Composite3D c3d = renderer.getC3D();
            final boolean singleVertex = !c3d.getLockableDatFileReference().isReadOnly() && c3d.getLockableDatFileReference().getVertexManager().getSelectedVertices().size() == 1;

            if (singleVertex) {
                Editor3DWindow.getWindow().getVertexWindow().renew();
            }

            if (singleVertex && Editor3DWindow.getWindow().getVertexWindow().getShell() == null) {
                Editor3DWindow.getWindow().getVertexWindow().run();
            } else if (!singleVertex && Editor3DWindow.getWindow().getVertexWindow().getShell() != null) {
                Editor3DWindow.getWindow().getVertexWindow().close();
            }
        }

        if (vertexWindow.getShell() == null) {
            return;
        }
        if (lastHoveredC3d != null) {
            Point a = ShellHelper.absolutePositionOnShell(lastHoveredC3d);
            Point s = vertexWindow.getShell().getSize();
            vertexWindow.getShell().setLocation(a.x - s.x + lastHoveredC3d.getSize().x, a.y);
        }
    }


    @Override
    protected Control createContents(Composite parent) {
        final Composite vertexWindow = new Composite(parent, SWT.BORDER);
        GridLayout gridLayout = new GridLayout();
        gridLayout.verticalSpacing = -2;
        gridLayout.horizontalSpacing = 1;
        vertexWindow.setLayout(gridLayout);

        {
            final String NUMBER_FORMAT = View.NUMBER_FORMAT8F;

            {
                Composite cmp_txt = new Composite(vertexWindow, SWT.NONE);
                cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmp_txt.setLayout(new GridLayout(5, true));

                Label lbl_vertexData = new Label(cmp_txt, SWT.NONE);
                lbl_vertexData.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
                lbl_vertexData.setText("Vertex data:"); //$NON-NLS-1$ I18N Needs translation!

                {
                    NButton btn_Copy = new NButton(cmp_txt, Cocoa.getStyle());
                    btn_Copy.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
                    btn_Copy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
                    KeyStateManager.addTooltipText(btn_Copy, I18n.COPYNPASTE_Copy, Task.COPY);
                }
                {
                    NButton btn_Paste = new NButton(cmp_txt, Cocoa.getStyle());
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
                spn_X.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                spn_X.setMaximum(new BigDecimal(1000000));
                spn_X.setMinimum(new BigDecimal(-1000000));
                spn_X.setValue(BigDecimal.ZERO);
            }

            {
                Composite cmp_txt = new Composite(vertexWindow, SWT.NONE);
                cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmp_txt.setLayout(new GridLayout(1, true));

                BigDecimalSpinner spn_Y = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
                spn_Y.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                spn_Y.setMaximum(new BigDecimal(1000000));
                spn_Y.setMinimum(new BigDecimal(-1000000));
                spn_Y.setValue(BigDecimal.ZERO);
            }

            {
                Composite cmp_txt = new Composite(vertexWindow, SWT.NONE);
                cmp_txt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
                cmp_txt.setLayout(new GridLayout(1, true));

                BigDecimalSpinner spn_Z = new BigDecimalSpinner(cmp_txt, SWT.NONE, NUMBER_FORMAT);
                spn_Z.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
                spn_Z.setMaximum(new BigDecimal(1000000));
                spn_Z.setMinimum(new BigDecimal(-1000000));
                spn_Z.setValue(BigDecimal.ZERO);
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
}
