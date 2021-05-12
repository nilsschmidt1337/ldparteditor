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

import java.util.Set;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Point;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.helper.composite3d.RectifierSettings;
import org.nschmidt.ldparteditor.helper.compositetext.Text2SelectionConverter;
import org.nschmidt.ldparteditor.helper.math.HashBiMap;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;

/**
 * Splits selected quads into triangles
 *
 * @author nils
 *
 */
public enum QuadMerger {
    INSTANCE;

    public static void mergeTrianglesIntoQuad(int lineStart, int lineEnd, DatFile datFile) {

        if (datFile.isReadOnly())
            return;

        final VertexManager vm = datFile.getVertexManager();
        vm.backupHideShowState();
        vm.backupSelection();
        vm.clearSelection();

        Text2SelectionConverter.convert(lineStart, lineEnd, datFile);

        final Set<GData> sd = datFile.getVertexManager().getSelectedData();
        HashBiMap<Integer, GData> dpl = datFile.getDrawPerLineNoClone();

        lineEnd += 1;
        for (int line = lineStart; line < lineEnd; line++) {
            sd.add(dpl.getValue(line));
        }

        RectifierSettings rs = new RectifierSettings();
        rs.setScope(1);
        rs.setNoBorderedQuadToRectConversation(true);
        vm.rectify(rs, false, false);

        vm.restoreSelection();
        vm.restoreHideShowState();
        vm.setModifiedNoSync();
        for (EditorTextWindow w : Project.getOpenTextWindows()) {
            for (CTabItem t : w.getTabFolder().getItems()) {
                if (datFile.equals(((CompositeTab) t).getState().getFileNameObj())) {
                    ((CompositeTab) t).getState().setSync(true);
                    Point s = ((CompositeTab) t).getTextComposite().getSelection();
                    ((CompositeTab) t).getTextComposite().setText(datFile.getText());
                    ((CompositeTab) t).getTextComposite().setSelection(s);
                    ((CompositeTab) t).getState().setSync(false);
                    ((CompositeTab) t).parseForErrorAndHints();
                    ((CompositeTab) t).getTextComposite().redraw();
                    break;
                }
            }
        }
        vm.skipSyncTimer();
        vm.setModified(true, true);
    }
}
