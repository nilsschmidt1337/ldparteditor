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

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.widget.NButton;

public class HideUnhideToolItem extends ToolItem {

    private final NButton[] btnHidePtr = new NButton[1];
    private final NButton[] btnShowAllPtr = new NButton[1];

    public HideUnhideToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);

        NButton btnHide = new NButton(this, Cocoa.getStyle());
        this.btnHidePtr[0] = btnHide;
        btnHide.setToolTipText(I18n.E3D_HIDE);
        btnHide.setImage(ResourceManager.getImage("icon16_hide.png")); //$NON-NLS-1$

        NButton btnUnhide = new NButton(this, Cocoa.getStyle());
        this.btnShowAllPtr[0] = btnUnhide;
        btnUnhide.setToolTipText(I18n.E3D_SHOW_ALL);
        btnUnhide.setImage(ResourceManager.getImage("icon16_unhide.png")); //$NON-NLS-1$

        addListeners();
    }

    private void addListeners() {
        widgetUtil(btnHidePtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null && !Project.getFileToEdit().getVertexManager().getSelectedData().isEmpty()) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().hideSelection();
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    for (CTabItem t : w.getTabFolder().getItems()) {
                        if (Project.getFileToEdit().equals(((CompositeTab) t).getState().getFileNameObj())) {
                            StyledText st = ((CompositeTab) t).getTextComposite();
                            st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
                        }
                    }
                }
                Project.getFileToEdit().addHistory();
            }

            Editor3DWindow.getWindow().regainFocus();
        });
        widgetUtil(btnShowAllPtr[0]).addSelectionListener(e -> {
            if (Project.getFileToEdit() != null) {
                Project.getFileToEdit().getVertexManager().addSnapshot();
                Project.getFileToEdit().getVertexManager().showAll();
                for (EditorTextWindow w : Project.getOpenTextWindows()) {
                    for (CTabItem t : w.getTabFolder().getItems()) {
                        if (Project.getFileToEdit().equals(((CompositeTab) t).getState().getFileNameObj())) {
                            StyledText st = ((CompositeTab) t).getTextComposite();
                            st.redraw(0, 0, st.getBounds().width, st.getBounds().height, true);
                        }
                    }
                }
                Project.getFileToEdit().addHistory();
            }

            Editor3DWindow.getWindow().regainFocus();
        });
    }
}
