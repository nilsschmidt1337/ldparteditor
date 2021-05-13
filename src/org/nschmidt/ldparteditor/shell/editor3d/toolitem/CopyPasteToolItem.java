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

import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.helper.composite3d.GuiStatusManager;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

public class CopyPasteToolItem extends ToolItem {

    public CopyPasteToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);

        NButton btnCut = new NButton(this, Cocoa.getStyle());
        btnCut.setImage(ResourceManager.getImage("icon16_edit-cut.png")); //$NON-NLS-1$
        KeyStateManager.addTooltipText(btnCut, I18n.COPYNPASTE_CUT, Task.CUT);

        NButton btnCopy = new NButton(this, Cocoa.getStyle());
        btnCopy.setImage(ResourceManager.getImage("icon16_edit-copy.png")); //$NON-NLS-1$
        KeyStateManager.addTooltipText(btnCopy, I18n.COPYNPASTE_COPY, Task.COPY);

        NButton btnPaste = new NButton(this, Cocoa.getStyle());
        btnPaste.setImage(ResourceManager.getImage("icon16_edit-paste.png")); //$NON-NLS-1$
        KeyStateManager.addTooltipText(btnPaste, I18n.COPYNPASTE_PASTE, Task.PASTE);

        NButton btnDelete = new NButton(this, Cocoa.getStyle());
        btnDelete.setImage(ResourceManager.getImage("icon16_delete.png")); //$NON-NLS-1$
        KeyStateManager.addTooltipText(btnDelete, I18n.COPYNPASTE_DELETE, Task.DELETE);

        widgetUtil(btnDelete).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().delete(Editor3DWindow.getWindow().isMovingAdjacentData(), true);
            }

            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnCopy).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().copy();
            }

            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnCut).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().copy();
                Project.getFileToEdit().getVertexManager().delete(false, true);
            }

            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnPaste).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().paste(MiscToolItem.loadSelectorSettings());
                if (WorkbenchManager.getUserSettingState().isDisableMAD3D()) {
                    Editor3DWindow.getWindow().setMovingAdjacentData(false);
                    GuiStatusManager.updateStatus();
                }
            }

            Editor3DWindow.getWindow().regainFocus();
        });
    }
}
