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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.composite.ToolItem;
import org.nschmidt.ldparteditor.data.GData1;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.ObjectMode;
import org.nschmidt.ldparteditor.enumtype.Task;
import org.nschmidt.ldparteditor.helper.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.resource.ResourceManager;
import org.nschmidt.ldparteditor.shell.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.state.KeyStateManager;
import org.nschmidt.ldparteditor.widget.NButton;

public class WorkingTypeToolItem extends ToolItem {

    private static final NButton[] btnVerticesPtr = new NButton[1];
    private static final NButton[] btnTrisNQuadsPtr = new NButton[1];
    private static final NButton[] btnLinesPtr = new NButton[1];
    private static final NButton[] btnSubfilesPtr = new NButton[1];

    private static ObjectMode workingType = ObjectMode.VERTICES;

    public WorkingTypeToolItem(Composite parent, int style, boolean isHorizontal) {
        super(parent, style, isHorizontal);
        createWidgets(this);
        addListeners();
    }

    public static void setObjMode(int type) {
        switch (type) {
        case 0:
            btnVerticesPtr[0].setSelection(true);
            setWorkingType(ObjectMode.VERTICES);
            clickSingleBtn(btnVerticesPtr[0]);
            break;
        case 1:
            btnTrisNQuadsPtr[0].setSelection(true);
            setWorkingType(ObjectMode.FACES);
            clickSingleBtn(btnTrisNQuadsPtr[0]);
            break;
        case 2:
            btnLinesPtr[0].setSelection(true);
            setWorkingType(ObjectMode.LINES);
            clickSingleBtn(btnLinesPtr[0]);
            break;
        case 3:
            btnSubfilesPtr[0].setSelection(true);
            setWorkingType(ObjectMode.SUBFILES);
            clickSingleBtn(btnSubfilesPtr[0]);
            break;
        default:
            break;
        }
    }

    public static ObjectMode getWorkingType() {
        return workingType;
    }

    private static void setWorkingType(ObjectMode workingMode) {
        workingType = workingMode;
    }

    private static void createWidgets(WorkingTypeToolItem workingTypeToolItem) {
        NButton btnVertices = new NButton(workingTypeToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnVerticesPtr[0] = btnVertices;
        KeyStateManager.addTooltipText(btnVertices, I18n.E3D_MODE_VERTEX + I18n.E3D_ALT_TO_DESELECT, Task.OBJ_VERTEX);
        btnVertices.setSelection(true);
        btnVertices.setImage(ResourceManager.getImage("icon16_vertices.png")); //$NON-NLS-1$

        NButton btnTrisNQuads = new NButton(workingTypeToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnTrisNQuadsPtr[0] = btnTrisNQuads;
        KeyStateManager.addTooltipText(btnTrisNQuads, I18n.E3D_MODE_SURFACE + I18n.E3D_ALT_TO_DESELECT, Task.OBJ_FACE);
        btnTrisNQuads.setImage(ResourceManager.getImage("icon16_trisNquads.png")); //$NON-NLS-1$

        NButton btnLines = new NButton(workingTypeToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnLinesPtr[0] = btnLines;
        KeyStateManager.addTooltipText(btnLines, I18n.E3D_MODE_LINE + I18n.E3D_ALT_TO_DESELECT, Task.OBJ_LINE);
        btnLines.setImage(ResourceManager.getImage("icon16_lines.png")); //$NON-NLS-1$

        NButton btnSubfiles = new NButton(workingTypeToolItem, SWT.TOGGLE | Cocoa.getStyle());
        btnSubfilesPtr[0] = btnSubfiles;
        KeyStateManager.addTooltipText(btnSubfiles, I18n.E3D_MODE_SUBPART + I18n.E3D_ALT_TO_DESELECT, Task.OBJ_PRIMITIVE);
        btnSubfiles.setImage(ResourceManager.getImage("icon16_primitives.png")); //$NON-NLS-1$
    }

    private static void addListeners() {
        widgetUtil(btnVerticesPtr[0]).addSelectionListener(e -> {
            clickRadioBtn(btnVerticesPtr[0]);
            setWorkingType(ObjectMode.VERTICES);
            if ((e.stateMask & SWT.ALT) == SWT.ALT && Project.getFileToEdit() != null && !Project.getFileToEdit().getVertexManager().getSelectedVertices().isEmpty()) {
                final VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.getSelectedVertices().clear();
                if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                    vm.reSelectSubFiles();
                } else {
                    vm.getSelectedData().removeAll(vm.getSelectedSubfiles());
                    vm.getSelectedSubfiles().clear();
                }
                vm.setModified(true, true);
            }
            regainFocus();
        });
        widgetUtil(btnTrisNQuadsPtr[0]).addSelectionListener(e -> {
            clickRadioBtn(btnTrisNQuadsPtr[0]);
            setWorkingType(ObjectMode.FACES);
            if ((e.stateMask & SWT.ALT) == SWT.ALT && Project.getFileToEdit() != null && (!Project.getFileToEdit().getVertexManager().getSelectedQuads().isEmpty() || !Project.getFileToEdit().getVertexManager().getSelectedTriangles().isEmpty())) {
                final VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.getSelectedData().removeAll(vm.getSelectedTriangles());
                vm.getSelectedData().removeAll(vm.getSelectedQuads());
                vm.getSelectedTriangles().clear();
                vm.getSelectedQuads().clear();
                if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                    vm.reSelectSubFiles();
                } else {
                    vm.getSelectedData().removeAll(vm.getSelectedSubfiles());
                    vm.getSelectedSubfiles().clear();
                }
                vm.setModified(true, true);
            }
            regainFocus();
        });
        widgetUtil(btnLinesPtr[0]).addSelectionListener(e -> {
            clickRadioBtn(btnLinesPtr[0]);
            setWorkingType(ObjectMode.LINES);
            if ((e.stateMask & SWT.ALT) == SWT.ALT && Project.getFileToEdit() != null && (!Project.getFileToEdit().getVertexManager().getSelectedLines().isEmpty() || !Project.getFileToEdit().getVertexManager().getSelectedCondlines().isEmpty())) {
                final VertexManager vm = Project.getFileToEdit().getVertexManager();
                vm.getSelectedData().removeAll(vm.getSelectedCondlines());
                vm.getSelectedData().removeAll(vm.getSelectedLines());
                vm.getSelectedCondlines().clear();
                vm.getSelectedLines().clear();
                if (Cocoa.checkCtrlOrCmdPressed(e.stateMask)) {
                    vm.reSelectSubFiles();
                } else {
                    vm.getSelectedData().removeAll(vm.getSelectedSubfiles());
                    vm.getSelectedSubfiles().clear();
                }
                vm.setModified(true, true);
            }
            regainFocus();
        });
        widgetUtil(btnSubfilesPtr[0]).addSelectionListener(e -> {
            clickRadioBtn(btnSubfilesPtr[0]);
            setWorkingType(ObjectMode.SUBFILES);
            if ((e.stateMask & SWT.ALT) == SWT.ALT && !Cocoa.checkCtrlOrCmdPressed(e.stateMask) && Project.getFileToEdit() != null && !Project.getFileToEdit().getVertexManager().getSelectedSubfiles().isEmpty()) {
                final VertexManager vm = Project.getFileToEdit().getVertexManager();
                final List<GData1> subfiles = new ArrayList<>();
                subfiles.addAll(vm.getSelectedSubfiles());
                for (GData1 g1 : subfiles) {
                    vm.removeSubfileFromSelection(g1);
                }
                vm.getSelectedSubfiles().clear();
                vm.setModified(true, true);
            }
            regainFocus();
        });
    }

    private static void regainFocus() {
        Editor3DWindow.getWindow().regainFocus();
    }
}
