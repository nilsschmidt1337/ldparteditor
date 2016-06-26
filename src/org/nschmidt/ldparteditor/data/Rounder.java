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
package org.nschmidt.ldparteditor.data;

import java.math.RoundingMode;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composites.compositetab.CompositeTabState;
import org.nschmidt.ldparteditor.helpers.compositetext.Text2SelectionConverter;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shells.editor3d.Editor3DWindow;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Rounds selected lines
 *
 * @author nils
 *
 */
public enum Rounder {
    INSTANCE;

    /**
     * Rounds selected lines (clears the selection)
     *
     * @param cText
     *            the selected CompositeText
     * @param lineStart
     *            start line number to round
     * @param lineEnd
     *            end line number to round
     * @param datFile
     */
    public static void round(CompositeTabState st, StyledText cText, int lineStart, int lineEnd, DatFile datFile) {

        if (datFile.isReadOnly())
            return;

        // Check here if single vertex replacing (ALT+SHIFT+R) is active
        // If so, round only this vertex!

        final VertexManager vm = datFile.getVertexManager();

        if (st != null && st.isReplacingVertex() && vm.getVertexToReplace() != null && vm.getVertices().contains(vm.getVertexToReplace())) {
            vm.clearSelection();
            Text2SelectionConverter.convert(lineStart, lineEnd, datFile);
            vm.skipSyncTimer();
            vm.backupHideShowState();
            GDataCSG.resetCSG(datFile, false);
            GDataCSG.forceRecompile(datFile);
            Vertex vOld = new Vertex(st.getToReplaceX(), st.getToReplaceY(), st.getToReplaceZ());
            int coordsDecimalPlaces = WorkbenchManager.getUserSettingState().getCoordsPrecision();
            Vertex vNew = new Vertex(vOld.X.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP), vOld.Y.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP), vOld.Z.setScale(coordsDecimalPlaces,
                    RoundingMode.HALF_UP));
            vm.changeVertexDirectFast(vOld, vNew, true);
            st.setToReplaceX(vNew.X);
            st.setToReplaceY(vNew.Y);
            st.setToReplaceZ(vNew.Z);
            vm.setVertexToReplace(vNew);
            vm.setModified_NoSync();
            datFile.getVertexManager().restoreHideShowState();
            datFile.getVertexManager().syncWithTextEditors(true);

            vm.updateUnsavedStatus();

        } else {

            Text2SelectionConverter.convert(lineStart, lineEnd, datFile);
            datFile.getVertexManager().skipSyncTimer();
            datFile.getVertexManager().backupHideShowState();
            datFile.getVertexManager()
            .roundSelection(WorkbenchManager.getUserSettingState().getCoordsPrecision(), WorkbenchManager.getUserSettingState().getTransMatrixPrecision(), Editor3DWindow.getWindow().isMovingAdjacentData(), true);
        }

        datFile.getVertexManager().clearSelection();
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            for (CTabItem t : w.getTabFolder().getItems()) {
                if (datFile.equals(((CompositeTab) t).getState().getFileNameObj())) {
                    ((CompositeTab) t).parseForErrorAndHints();
                    ((CompositeTab) t).getTextComposite().redraw();
                    break;
                }
            }
        }
    }
}
