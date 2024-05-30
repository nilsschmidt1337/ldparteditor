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
package org.nschmidt.ldparteditor.helper.compositetext;

import java.math.BigDecimal;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.data.Matrix;
import org.nschmidt.ldparteditor.data.VertexManager;
import org.nschmidt.ldparteditor.enumtype.Axis;
import org.nschmidt.ldparteditor.enumtype.RotationSnap;
import org.nschmidt.ldparteditor.enumtype.TransformationMode;
import org.nschmidt.ldparteditor.enumtype.View;
import org.nschmidt.ldparteditor.helper.Manipulator;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shell.editor3d.toolitem.MiscToggleToolItem;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;

/**
 * Globally rotates or translates the current selection
 */
public enum RotatorAndTranslator {
    INSTANCE;

    /**
     * Globally rotates or translates the current selection
     *
     * @param StyledText
     *            the selected StyledText
     * @param lineStart
     *            start line number to annotate
     * @param lineEnd
     *            end line number to annotate
     * @param datFile
     * @param axis
     * @param invert
     * @param mode
     */
    public static void moveOrRotate(StyledText st, int lineStart, int lineEnd, DatFile datFile, TransformationMode mode, boolean invert, Axis axis) {
        Text2SelectionConverter.convert(st, lineStart, lineEnd, datFile);
        final VertexManager vm = datFile.getVertexManager();
        vm.reSelectSubFiles();
        vm.backupHideShowState();
        vm.skipSyncTimer();

        // first, detect on which line the caret is (lineStart or lineEnd)
        final int currentCaretStartLine = st.getLineAtOffset(st.getSelection().x);
        final int currentCaretEndLine = st.getLineAtOffset(st.getSelection().y);
        // then detect the character delta to restore it later
        final int characterStartDelta = st.getSelection().x - st.getOffsetAtLine(currentCaretStartLine);
        final int characterEndDelta = st.getSelection().y - st.getOffsetAtLine(currentCaretEndLine);

        Matrix m = View.ACCURATE_ID;

        final BigDecimal[] snap = Manipulator.getSnap();
        final RotationSnap[] rotSnap = Manipulator.getRotationSnap();
        if (mode == TransformationMode.TRANSLATE) {
            if (axis == Axis.X) {
                m = m.translateGlobally(snap[0], BigDecimal.ZERO, BigDecimal.ZERO);
            } else if (axis == Axis.Y) {
                m = m.translateGlobally(BigDecimal.ZERO, snap[1], BigDecimal.ZERO);
            } else if (axis == Axis.Z) {
                m = m.translateGlobally(BigDecimal.ZERO, BigDecimal.ZERO, snap[2]);
            } else {
                return;
            }
        } else if (mode == TransformationMode.ROTATE){
            if (axis == Axis.X) {
                m = m.rotate(snap[3], rotSnap[0], new BigDecimal[] {BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO});
            } else if (axis == Axis.Y) {
                m = m.rotate(snap[4], rotSnap[1], new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ZERO});
            } else if (axis == Axis.Z) {
                m = m.rotate(snap[5], rotSnap[2], new BigDecimal[] {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE});
            } else {
                return;
            }
        } else {
            return;
        }

        if (invert) {
            m = m.invert();
        }

        vm.transformSelection(m, null, MiscToggleToolItem.isMovingAdjacentData(), true);

        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            for (CTabItem t : w.getTabFolder().getItems()) {
                if (datFile.equals(((CompositeTab) t).getState().getFileNameObj())) {
                    // We need to update the text now, otherwise the caret selection can't be restored.
                    vm.backupSelection();
                    st.setText(datFile.getText());
                    vm.restoreSelection();
                    st.setSelection(
                            st.getOffsetAtLine(currentCaretStartLine) + Math.min(st.getLine(currentCaretStartLine).length(), characterStartDelta),
                            st.getOffsetAtLine(currentCaretEndLine) + Math.min(st.getLine(currentCaretEndLine).length(), characterEndDelta));
                    ((CompositeTab) t).parseForErrorAndHints();
                    ((CompositeTab) t).getTextComposite().redraw();
                    break;
                }
            }
        }
    }
}
