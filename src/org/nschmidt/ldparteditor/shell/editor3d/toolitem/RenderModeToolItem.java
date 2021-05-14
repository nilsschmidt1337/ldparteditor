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

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.composite.Composite3D;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;

public class RenderModeToolItem extends ToolItem {

    public RenderModeToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);

        NButton btnRenderModeNoBackfaceCulling = new NButton(this, Cocoa.getStyle());
        KeyStateManager.addTooltipText(btnRenderModeNoBackfaceCulling, I18n.C3D_NO_BACKFACE_CULLING, Task.RENDERMODE_NO_BACKFACE_CULLING);
        btnRenderModeNoBackfaceCulling.setImage(ResourceManager.getImage("icon16_noBfc.png")); //$NON-NLS-1$

        NButton btnRenderModeRandomColours = new NButton(this, Cocoa.getStyle());
        KeyStateManager.addTooltipText(btnRenderModeRandomColours, I18n.C3D_RANDOM_COLOURS, Task.RENDERMODE_RANDOM_COLOURS);
        btnRenderModeRandomColours.setImage(ResourceManager.getImage("icon16_randomColour.png")); //$NON-NLS-1$

        NButton btnRenderModeGreenRed = new NButton(this, Cocoa.getStyle());
        KeyStateManager.addTooltipText(btnRenderModeGreenRed, I18n.C3D_GREEN_RED, Task.RENDERMODE_GREEN_FRONTFACES_RED_BACKFACES);
        btnRenderModeGreenRed.setImage(ResourceManager.getImage("icon16_greenFrontRedBack.png")); //$NON-NLS-1$

        NButton btnRenderModeRedBackfaces = new NButton(this, Cocoa.getStyle());
        KeyStateManager.addTooltipText(btnRenderModeRedBackfaces, I18n.C3D_RED_BACKFACES, Task.RENDERMODE_RED_BACKFACES);
        btnRenderModeRedBackfaces.setImage(ResourceManager.getImage("icon16_redBackfaces.png")); //$NON-NLS-1$

        NButton btnRenderModeRealBackfaceCulling = new NButton(this, Cocoa.getStyle());
        KeyStateManager.addTooltipText(btnRenderModeRealBackfaceCulling, I18n.C3D_REAL_BACKFACE_CULLING, Task.RENDERMODE_REAL_BACKFACE_CULLING);
        btnRenderModeRealBackfaceCulling.setImage(ResourceManager.getImage("icon16_realBfc.png")); //$NON-NLS-1$

        NButton btnRenderModeLDrawStandard = new NButton(this, Cocoa.getStyle());
        KeyStateManager.addTooltipText(btnRenderModeLDrawStandard, I18n.C3D_LDRAW_STANDARD, Task.RENDERMODE_LDRAW_STANDARD);
        btnRenderModeLDrawStandard.setImage(ResourceManager.getImage("icon16_ldrawStandard.png")); //$NON-NLS-1$

        NButton btnRenderModeCondlineMode = new NButton(this, Cocoa.getStyle());
        KeyStateManager.addTooltipText(btnRenderModeCondlineMode, I18n.C3D_CONDLINE_MODE, Task.RENDERMODE_SPECIAL_CONDLINE);
        btnRenderModeCondlineMode.setImage(ResourceManager.getImage("icon16_specialCondline.png")); //$NON-NLS-1$

        NButton btnRenderModeCoplanarityMode = new NButton(this, Cocoa.getStyle());
        KeyStateManager.addTooltipText(btnRenderModeCoplanarityMode, I18n.C3D_COPLANARITY_MODE, Task.RENDERMODE_COPLANARITY_HEATMAP);
        btnRenderModeCoplanarityMode.setImage(ResourceManager.getImage("icon16_coplanarityHeatmap.png")); //$NON-NLS-1$

        NButton btnRenderModeWireframe = new NButton(this, Cocoa.getStyle());
        KeyStateManager.addTooltipText(btnRenderModeWireframe, I18n.C3D_WIREFRAME, Task.RENDERMODE_WIREFRAME);
        btnRenderModeWireframe.setImage(ResourceManager.getImage("icon16_wireframe.png")); //$NON-NLS-1$

        widgetUtil(btnRenderModeNoBackfaceCulling).addSelectionListener(this::setRenderModeNoBackfaceCulling);
        widgetUtil(btnRenderModeRandomColours).addSelectionListener(this::setRenderModeRandomColours);
        widgetUtil(btnRenderModeGreenRed).addSelectionListener(this::setRenderModeGreenRed);
        widgetUtil(btnRenderModeRedBackfaces).addSelectionListener(this::setRenderModeRedBackfaces);
        widgetUtil(btnRenderModeRealBackfaceCulling).addSelectionListener(this::setRenderModeRealBackfaceCulling);
        widgetUtil(btnRenderModeLDrawStandard).addSelectionListener(this::setRenderModeLDrawStandard);
        widgetUtil(btnRenderModeCondlineMode).addSelectionListener(this::setRenderModeCondlineMode);
        widgetUtil(btnRenderModeCoplanarityMode).addSelectionListener(this::setRenderModeCoplanarityMode);
        widgetUtil(btnRenderModeWireframe).addSelectionListener(this::setRenderModeWireframe);
    }

    private void setRenderModeNoBackfaceCulling(SelectionEvent e) {
        setRenderMode(0);
    }

    private void setRenderModeRandomColours(SelectionEvent e) {
        setRenderMode(1);
    }

    private void setRenderModeGreenRed(SelectionEvent e) {
        setRenderMode(2);
    }

    private void setRenderModeRedBackfaces(SelectionEvent e) {
        setRenderMode(3);
    }

    private void setRenderModeRealBackfaceCulling(SelectionEvent e) {
        setRenderMode(4);
    }

    private void setRenderModeLDrawStandard(SelectionEvent e) {
        setRenderMode(5);
    }

    private void setRenderModeCondlineMode(SelectionEvent e) {
        setRenderMode(6);
    }

    private void setRenderModeCoplanarityMode(SelectionEvent e) {
        setRenderMode(7);
    }

    private void setRenderModeWireframe(SelectionEvent e) {
        setRenderMode(-1);
    }

    private void setRenderMode(int mode) {
        Composite3D c3d = Editor3DWindow.getWindow().getCurrentCoposite3d();
        if (c3d != null) {
            c3d.setRenderMode(mode);
            c3d.setRenderModeOnContextMenu(mode);
        }
        Editor3DWindow.getWindow().regainFocus();
    }
}
