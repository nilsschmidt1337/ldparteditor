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
package org.nschmidt.ldparteditor.composites;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ControlEditor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.opengl.GLCanvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nschmidt.ldparteditor.enums.Task;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widgets.BigDecimalSpinner;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * This is the vertex windows which allows the modification of a selected single vertex.
 *
 * @author nils
 *
 */
public class CompositeVertexWindow extends Composite{

    private Composite vertexWindow;
    private ControlEditor editor;

    /**
     * Creates a new vertex window
     * @param canvas the {@linkplain GLCanvas} from the underlying 3D composite.
     * @param style the style of this window.
     */
    public CompositeVertexWindow(Composite canvas, int style) {
        super(canvas, style);
        editor = new ControlEditor(canvas);
        editor.horizontalAlignment = SWT.RIGHT;
        editor.verticalAlignment = SWT.TOP;
        editor.grabHorizontal = false;
        editor.grabVertical = false;

        vertexWindow = new Composite(canvas, SWT.BORDER);
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

            vertexWindow.pack();
        }

        Point size = vertexWindow.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        editor.minimumWidth = size.x;
        editor.minimumHeight = size.y;
        editor.setEditor(vertexWindow);
    }

    @Override
    public void dispose() {
        super.dispose();
        vertexWindow.dispose();
        editor.dispose();
    }
}
