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
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;

public class UndoRedoToolItem extends ToolItem {

    public UndoRedoToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);

        NButton btnUndo = new NButton(this, Cocoa.getStyle());
        btnUndo.setImage(ResourceManager.getImage("icon16_undo.png")); //$NON-NLS-1$
        KeyStateManager.addTooltipText(btnUndo, I18n.E3D_UNDO, Task.UNDO);

        if (NLogger.debugging) {
            NButton btnSnapshot = new NButton(this, Cocoa.getStyle());
            btnSnapshot.setImage(ResourceManager.getImage("icon16_snapshot.png")); //$NON-NLS-1$
            btnSnapshot.setToolTipText(I18n.E3D_SNAPSHOT);

            widgetUtil(btnSnapshot).addSelectionListener(e -> {
                if (Project.getFileToEdit() != null) {
                    Project.getFileToEdit().getVertexManager().addSnapshot();
                    Project.getFileToEdit().addHistory();
                }
            });
        }

        NButton btnRedo = new NButton(this, Cocoa.getStyle());
        btnRedo.setImage(ResourceManager.getImage("icon16_redo.png")); //$NON-NLS-1$
        KeyStateManager.addTooltipText(btnRedo, I18n.E3D_REDO, Task.REDO);

        widgetUtil(btnUndo).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().undo(false);
            }

            Editor3DWindow.getWindow().regainFocus();
        });

        widgetUtil(btnRedo).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().redo(false);
            }

            Editor3DWindow.getWindow().regainFocus();
        });
    }
}
