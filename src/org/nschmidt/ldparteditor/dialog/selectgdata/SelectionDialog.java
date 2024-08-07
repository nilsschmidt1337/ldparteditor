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
package org.nschmidt.ldparteditor.dialog.selectgdata;

import java.util.Collection;
import org.eclipse.swt.widgets.Shell;
import org.nschmidt.ldparteditor.logger.NLogger;

/**
 *
 * <p>
 * Note: This class should be instantiated, it defines all listeners and part of
 * the business logic. It overrides the {@code open()} method to invoke the
 * listener definitions ;)
 */
public class SelectionDialog<T> extends SelectionDesign<T> {

    private T selection;

    /**
     * Create the dialog.
     *
     * @param parentShell
     */
    public SelectionDialog(Shell parentShell, String title, String icon) {
        super(parentShell, title, icon);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int open() {
        super.create();
        // MARK All final listeners will be configured here..
        treePtr[0].addSelectionListener(ev -> {
            selection = (T) treePtr[0].getSelection()[0].getData();
            NLogger.debug(SelectionDialog.class, "Selected data: {0}", selection); //$NON-NLS-1$
        });
        return super.open();
    }

    public void addData(Collection<T> selection) {
        selectionData.addAll(selection);
    }

    public T getSelection() {
        return selection;
    }
}
