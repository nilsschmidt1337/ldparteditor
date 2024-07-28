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
package org.nschmidt.ldparteditor.shell.editor3d.toolitem;

import static org.nschmidt.ldparteditor.helper.WidgetUtility.widgetUtil;

import java.math.BigDecimal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.dialog.value.ValueDialog;
import org.nschmidt.ldparteditor.enumtype.GL20Primitives;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.SphereGL20;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class LineThicknessToolItem extends ToolItem {

    private static volatile boolean hiQualityEdges = !WorkbenchManager.getUserSettingState().isDrawLinesOpenGL();

    private static final NButton[] btnToggleLinesOpenGLPtr = new NButton[1];
    private static final NButton[] btnLineSize0Ptr = new NButton[1];
    private static final NButton[] btnLineSize1Ptr = new NButton[1];
    private static final NButton[] btnLineSize2Ptr = new NButton[1];
    private static final NButton[] btnLineSize3Ptr = new NButton[1];
    private static final NButton[] btnLineSize4Ptr = new NButton[1];
    private static final NButton[] btnLineSize5Ptr = new NButton[1];

    public LineThicknessToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    private static void createWidgets(LineThicknessToolItem lineThicknessToolItem) {
        NButton btnLineSize0 = new NButton(lineThicknessToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnLineSize0Ptr[0] = btnLineSize0;
        btnLineSize0.setToolTipText(I18n.E3D_LINE_SIZE_0 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));
        btnLineSize0.setImage(ResourceManager.getImage("icon16_linesize0.png")); //$NON-NLS-1$

        NButton btnLineSize1 = new NButton(lineThicknessToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnLineSize1Ptr[0] = btnLineSize1;
        btnLineSize1.setToolTipText(I18n.E3D_LINE_SIZE_1 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));
        btnLineSize1.setImage(ResourceManager.getImage("icon16_linesize1.png")); //$NON-NLS-1$

        NButton btnLineSize2 = new NButton(lineThicknessToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnLineSize2Ptr[0] = btnLineSize2;
        btnLineSize2.setToolTipText(I18n.E3D_LINE_SIZE_2 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));
        btnLineSize2.setImage(ResourceManager.getImage("icon16_linesize2.png")); //$NON-NLS-1$

        NButton btnLineSize3 = new NButton(lineThicknessToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnLineSize3.setSelection(true);
        btnLineSize3Ptr[0] = btnLineSize3;
        btnLineSize3.setToolTipText(I18n.E3D_LINE_SIZE_3 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));
        btnLineSize3.setImage(ResourceManager.getImage("icon16_linesize3.png")); //$NON-NLS-1$

        NButton btnLineSize4 = new NButton(lineThicknessToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnLineSize4Ptr[0] = btnLineSize4;
        btnLineSize4.setToolTipText(I18n.E3D_LINE_SIZE_4 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));
        btnLineSize4.setImage(ResourceManager.getImage("icon16_linesize4.png")); //$NON-NLS-1$

        NButton btnLineSize5 = new NButton(lineThicknessToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnLineSize5Ptr[0] = btnLineSize5;
        btnLineSize5.setToolTipText(I18n.E3D_LINE_SIZE_5 + Cocoa.replaceCtrlByCmd(I18n.E3D_CONTROL_CLICK_MODIFY));
        btnLineSize5.setImage(ResourceManager.getImage("icon16_linesize5.png")); //$NON-NLS-1$

        NButton btnToggleLinesOpenGL = new NButton(lineThicknessToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnToggleLinesOpenGLPtr[0] = btnToggleLinesOpenGL;
        btnToggleLinesOpenGL.setToolTipText(I18n.E3D_LINE_OPENGL);
        btnToggleLinesOpenGL.setImage(ResourceManager.getImage("icon16_gllines.png")); //$NON-NLS-1$
        btnToggleLinesOpenGL.setSelection(!hiQualityEdges);
    }

    private static void addListeners() {
        widgetUtil(btnToggleLinesOpenGLPtr[0]).addSelectionListener(e -> {
            hiQualityEdges = !btnToggleLinesOpenGLPtr[0].getSelection();
            WorkbenchManager.getUserSettingState().setDrawLinesOpenGL(!hiQualityEdges);
            if (btnToggleLinesOpenGLPtr[0].getSelection()) {
                View.edgeThreshold = 5e6f;
            } else {
                View.edgeThreshold = 2e-6f;
            }
            regainFocus();
        });
        widgetUtil(btnLineSize0Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE0, GL20Primitives.SPHERE_INV0, 0f, 0f, 0.01f, btnLineSize0Ptr[0], e);
            regainFocus();
        });
        widgetUtil(btnLineSize1Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE1, GL20Primitives.SPHERE_INV1, 25f, .025f, 1f, btnLineSize1Ptr[0], e);
            regainFocus();
        });
        widgetUtil(btnLineSize2Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE2, GL20Primitives.SPHERE_INV2, 50f, .050f, 2f, btnLineSize2Ptr[0], e);
            regainFocus();
        });
        widgetUtil(btnLineSize3Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE3, GL20Primitives.SPHERE_INV3, 100f, .100f, 3f, btnLineSize3Ptr[0], e);
            regainFocus();
        });
        widgetUtil(btnLineSize4Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE4, GL20Primitives.SPHERE_INV4, 200f, .200f, 4f, btnLineSize4Ptr[0], e);
            regainFocus();
        });
        widgetUtil(btnLineSize5Ptr[0]).addSelectionListener(e -> {
            setLineSize(GL20Primitives.SPHERE5, GL20Primitives.SPHERE_INV5, 400f, .400f, 5f, btnLineSize5Ptr[0], e);
            regainFocus();
        });
        setLineSize(GL20Primitives.SPHERE3, GL20Primitives.SPHERE_INV3, 100f, .100f, 3f, btnLineSize3Ptr[0], null);
    }

    private static void setLineSize(SphereGL20 sp, SphereGL20 spInv, float lineWidth1000, float lineWidth, float lineWidthGL, NButton btn, SelectionEvent e) {
        if (e != null && Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
            new ValueDialog(Editor3DWindow.getWindow().getShell(), I18n.E3D_LINE_SCALE, I18n.E3D_LINE_SCALE_EXPLANATION) {

                @Override
                public void initializeSpinner() {
                    this.spnValuePtr[0].setMinimum(new BigDecimal("1")); //$NON-NLS-1$
                    this.spnValuePtr[0].setMaximum(new BigDecimal("10")); //$NON-NLS-1$
                    this.spnValuePtr[0].setValue(BigDecimal.valueOf(Math.max(WorkbenchManager.getUserSettingState().getLineScaleFactor(), 1f)));
                }

                @Override
                public void applyValue() {
                    WorkbenchManager.getUserSettingState().setLineScaleFactor(this.spnValuePtr[0].getValue().floatValue());
                }
            }.open();
        }

        final float lineScaleFactor = Math.max(WorkbenchManager.getUserSettingState().getLineScaleFactor(), 1f);
        final boolean useLegacyGL = WorkbenchManager.getUserSettingState().getOpenGLVersion() == 20;
        View.lineWidth1000 = lineWidth1000 * lineScaleFactor;
        View.lineWidth = lineWidth * lineScaleFactor;
        View.lineWidthGL = lineWidthGL * lineScaleFactor;
        if (useLegacyGL) {
            if (lineScaleFactor > 1f) {
                sp = new SphereGL20(View.lineWidth1000, 8);
                spInv = new SphereGL20(-View.lineWidth1000, 8);
            }

            GL20Primitives.sphere = sp;
            GL20Primitives.sphereInv = spInv;
        }

        Editor3DWindow.getWindow().updateLineThickness();

        if (btnToggleLinesOpenGLPtr[0] != null && btnToggleLinesOpenGLPtr[0].getSelection()) {
            View.edgeThreshold = 5e6f;
        } else {
            // Intentional quadratic factor
            View.edgeThreshold = 2e-6f / lineScaleFactor / lineScaleFactor;
        }

        if (e != null) {
            boolean openGLLines = false;
            if (btnToggleLinesOpenGLPtr[0] != null)
                openGLLines = btnToggleLinesOpenGLPtr[0].getSelection();
            clickSingleBtn(btn);
            btn.setSelection(true);
            if (btnToggleLinesOpenGLPtr[0] != null)
                btnToggleLinesOpenGLPtr[0].setSelection(openGLLines);
        }
    }

    private static void regainFocus() {
        Editor3DWindow.getWindow().regainFocus();
    }

    public static boolean hasHiQualityEdges() {
        return hiQualityEdges;
    }
}
