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
package org.nschmidt.ldparteditor.shells.editortext;

import org.eclipse.swt.widgets.Composite;

/**
 * Interface class to decouple the EditorTextWindow from the logic of the text editor
 * @author nilss
 *
 */
public class CompositeEditorText extends CompositeEditorTextDesign {
    
    private final boolean hasTextEditorWindow;
    private EditorTextWindow textEditorWindow;

    public CompositeEditorText(Composite parent, int style, boolean hasTextEditorWindow) {
        super(parent, style, hasTextEditorWindow);        
        this.hasTextEditorWindow = hasTextEditorWindow;
        // TODO Auto-generated constructor stub
    }

    public boolean hasTextEditorWindow() {
        return hasTextEditorWindow;
    }

    public EditorTextWindow getTextEditorWindow() {
        return textEditorWindow;
    }

    public void setTextEditorWindow(EditorTextWindow textEditorWindow) {
        this.textEditorWindow = textEditorWindow;
    }

}
