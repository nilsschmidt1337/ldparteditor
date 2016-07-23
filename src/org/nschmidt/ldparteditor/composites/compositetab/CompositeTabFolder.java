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
package org.nschmidt.ldparteditor.composites.compositetab;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.nschmidt.ldparteditor.shells.editortext.EditorTextWindow;

/**
 * @author nils
 *
 */
public class CompositeTabFolder extends CTabFolder {

    /** The editor reference */
    private EditorTextWindow window;

    public CompositeTabFolder(Composite parent, int style) {
        super(parent, style);
    }

    /**
     * @return the window of the text editor
     */
    public EditorTextWindow getWindow() {
        return window;
    }

    /**
     * @param window
     *            the text editor window to set
     */
    public void setWindow(EditorTextWindow window) {
        this.window = window;
    }
    
    public void cut() {
        CompositeTab selection = (CompositeTab) this.getSelection();
        if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
            final StyledText ct = selection.getTextComposite();
            if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                ct.copy();
                return;
            }
            final int x = ct.getSelection().x;
            if (ct.getSelection().y == x) {
                final int start = ct.getOffsetAtLine(ct.getLineAtOffset(x));
                ct.setSelection(start, start + ct.getLine(ct.getLineAtOffset(x)).length());
            }
            final int x2 = ct.getSelection().x;
            if (ct.getSelection().y == x2) {
                ct.forceFocus();
                return;
            }
            ct.cut();
            ct.forceFocus();
        }
    }

    public void copy() {
        CompositeTab selection = (CompositeTab) this.getSelection();
        if (selection != null) {
            final StyledText ct = selection.getTextComposite();
            final int x = ct.getSelection().x;
            if (ct.getSelection().y == x) {
                final int start = ct.getOffsetAtLine(ct.getLineAtOffset(x));
                ct.setSelection(start, start + ct.getLine(ct.getLineAtOffset(x)).length());
            }
            ct.copy();
            ct.forceFocus();
        }
    }

    public void paste() {
        CompositeTab selection = (CompositeTab) this.getSelection();
        if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
            if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                return;
            }
            final StyledText ct = selection.getTextComposite();
            ct.paste();
            ct.forceFocus();
        }
    }

    public void delete() {
        CompositeTab selection = (CompositeTab) this.getSelection();
        if (selection != null && !selection.getState().getFileNameObj().isReadOnly()) {
            if (!selection.getState().getFileNameObj().getVertexManager().isUpdated()){
                return;
            }
            final StyledText ct = selection.getTextComposite();
            final int x = ct.getSelection().x;
            if (ct.getSelection().y == x) {
                final int start = ct.getOffsetAtLine(ct.getLineAtOffset(x));
                ct.setSelection(start, start + ct.getLine(ct.getLineAtOffset(x)).length());
            }
            final int x2 = ct.getSelection().x;
            if (ct.getSelection().y == x2) {
                ct.forceFocus();
                return;
            }
            ct.insert(""); //$NON-NLS-1$
            ct.setSelection(new Point(x, x));
            ct.forceFocus();
        }
    }

}
