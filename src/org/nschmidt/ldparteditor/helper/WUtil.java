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
package org.nschmidt.ldparteditor.helper;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Widget;
import org.nschmidt.ldparteditor.composite.compositetab.CompositeTabFolder;
import org.nschmidt.ldparteditor.logger.NLogger;
import org.nschmidt.ldparteditor.widget.NButton;

/**
 * Utility function wrapper for controls and widgets (e.g. lambda expression syntax sugar)
 */
public class WUtil {

    private static final String NO_MAPPING_FOR_TYPE = "No mapping for type:"; //$NON-NLS-1$
    private final Widget w;

    WUtil(Widget w) {
        this.w = w;
    }

    public void addSelectionListener(final WidgetSelectionListener listener) {
        if (w instanceof NButton nbutton) {
            nbutton.addSelectionListener(listener);
        } else if (w instanceof Combo combo) {
            combo.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    listener.widgetSelected(e);
                }
            });
        } else if (w instanceof Button button) {
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    listener.widgetSelected(e);
                }
            });
        } else if (w instanceof MenuItem menuitem) {
            menuitem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    listener.widgetSelected(e);
                }
            });
        } else if (w instanceof CompositeTabFolder compositetabfolder) {
            compositetabfolder.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    listener.widgetSelected(e);
                }
            });
        } else if (w instanceof CTabFolder ctabfolder) {
            ctabfolder.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    listener.widgetSelected(e);
                }
            });
        } else if (w instanceof ScrollBar scrollbar) {
            scrollbar.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    listener.widgetSelected(e);
                }
            });
        } else if (w != null){
            NLogger.error(getClass(), NO_MAPPING_FOR_TYPE + w.getClass().getName());
        }
    }

    public void setItems(String... items) {
        if (w instanceof Combo combo) {
            combo.setItems(items);
        } else if (w != null){
            NLogger.error(getClass(), NO_MAPPING_FOR_TYPE + w.getClass().getName());
        }
    }

    public void setTransfer(Transfer... transferAgents) {
        if (w instanceof DragSource dragsource) {
            dragsource.setTransfer(transferAgents);
        } else if (w instanceof DropTarget droptarget) {
            droptarget.setTransfer(transferAgents);
        } else if (w != null){
            NLogger.error(getClass(), NO_MAPPING_FOR_TYPE + w.getClass().getName());
        }
    }
}
