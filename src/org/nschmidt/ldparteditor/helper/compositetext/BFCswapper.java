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

import org.eclipse.swt.custom.CTabItem;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTab;
import org.nschmidt.ldparteditor.data.DatFile;
import org.nschmidt.ldparteditor.project.Project;
import org.nschmidt.ldparteditor.shell.editortext.EditorTextWindow;

/**
 * Swaps the BFC winding of the current selection
 *
 * @author nils
 *
 */
public enum BFCswapper {
    INSTANCE;

    /**
     * Swaps the BFC winding of the current selection
     *
     * @param cText
     *            the selected CompositeText
     * @param lineStart
     *            start line number to annotate
     * @param lineEnd
     *            end line number to annotate
     * @param datFile
     */
    public static void swap(int lineStart, int lineEnd, DatFile datFile) {
        Text2SelectionConverter.convert(lineStart, lineEnd, datFile);
        datFile.getVertexManager().backupHideShowState();
        datFile.getVertexManager().skipSyncTimer();
        datFile.getVertexManager().windingChangeSelection(true);
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
