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
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTabState;
import org.nschmidt.ldparteditor.helper.compositetext.Text2SelectionConverter;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToggleToolItem;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;
import org.nschmidt.ldparteditor.workbench.UserSettingState;
import org.nschmidt.ldparteditor.workbench.WorkbenchManager;

/**
 * Rounds selected lines
 */
public enum Rounder {
    INSTANCE;

    /**
     * Rounds selected lines (clears the selection)
     * @param lineStart
     *            start line number to round
     * @param lineEnd
     *            end line number to round
     * @param datFile
     */
    public static void round(StyledText st, CompositeTabState cts, int lineStart, int lineEnd, DatFile datFile) {

        if (datFile.isReadOnly())
            return;

        // first, detect on which line the caret is (lineStart or lineEnd)
        final int currentCaretLine = st.getLineAtOffset(st.getCaretOffset());
        // then detect the character delta to restore it later
        final int characterDelta = st.getCaretOffset() - st.getOffsetAtLine(currentCaretLine);

        // Check here if single vertex replacing (ALT+SHIFT+R) is active
        // If so, round only this vertex!

        final VertexManager vm = datFile.getVertexManager();
        final UserSettingState userSettings = WorkbenchManager.getUserSettingState();
        final boolean onX = userSettings.isRoundX();
        final boolean onY = userSettings.isRoundY();
        final boolean onZ = userSettings.isRoundZ();

        if (cts != null && cts.isReplacingVertex() && vm.getVertexToReplace() != null && vm.getVertices().contains(vm.getVertexToReplace())) {
            vm.clearSelection();
            Text2SelectionConverter.convert(st, lineStart, lineEnd, datFile);
            vm.skipSyncTimer();
            vm.backupHideShowState();
            GDataCSG.resetCSG(datFile, false);
            GDataCSG.forceRecompile(datFile);
            Vertex vOld = new Vertex(cts.getToReplaceX(), cts.getToReplaceY(), cts.getToReplaceZ());
            int coordsDecimalPlaces = userSettings.getCoordsPrecision();
            Vertex vNew = new Vertex(onX ? vOld.xp.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP) : vOld.xp, onY ? vOld.yp.setScale(coordsDecimalPlaces, RoundingMode.HALF_UP) : vOld.yp, onZ ? vOld.zp.setScale(coordsDecimalPlaces,
                    RoundingMode.HALF_UP) : vOld.zp);
            vm.changeVertexDirectFast(vOld, vNew, true);
            cts.setToReplaceX(vNew.xp);
            cts.setToReplaceY(vNew.yp);
            cts.setToReplaceZ(vNew.zp);
            vm.setVertexToReplace(vNew);
            vm.setModifiedNoSync();
            datFile.getVertexManager().restoreHideShowState();
            datFile.getVertexManager().syncWithTextEditors(true);

            vm.updateUnsavedStatus();

        } else {

            Text2SelectionConverter.convert(st, lineStart, lineEnd, datFile);
            datFile.getVertexManager().skipSyncTimer();
            datFile.getVertexManager().backupHideShowState();
            datFile.getVertexManager()
            .roundSelection(userSettings.getCoordsPrecision(), userSettings.getTransMatrixPrecision(), MiscToggleToolItem.isMovingAdjacentData(), true, onX, onY, onZ);
        }

        datFile.getVertexManager().clearSelection();
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            for (CTabItem t : w.getTabFolder().getItems()) {
                if (datFile.equals(((CompositeTab) t).getState().getFileNameObj())) {
                    // We need to update the text now, otherwise the caret selection can't be restored.
                    CompositeTab tab = (CompositeTab) t;
                    StyledText textComposite = tab.getTextComposite();
                    final int safeCaretLine = Math.min(currentCaretLine, textComposite.getLineCount() - 1);
                    tab.getState().setSync(true);
                    textComposite.setText(datFile.getText());
                    try {
                        textComposite.setSelection(textComposite.getOffsetAtLine(safeCaretLine) + Math.min(textComposite.getLine(safeCaretLine).length(), characterDelta));
                    } catch (IllegalArgumentException iae) {
                        NLogger.debug(Rounder.class, iae);
                    }
                    tab.getState().setSync(false);
                    tab.parseForErrorAndHints();
                    textComposite.redraw();
                    break;
                }
            }
        }
    }
}
