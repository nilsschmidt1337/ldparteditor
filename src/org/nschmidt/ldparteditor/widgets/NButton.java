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
package org.nschmidt.ldparteditor.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author nils
 *
 */
public class NButton extends Canvas {

    private final List<SelectionListener> selectors = new ArrayList<>();
    private final List<PaintListener> painters = new ArrayList<>();

    private Image img = null;
    private String text = ""; //$NON-NLS-1$

    public NButton(Composite parent, int style) {
        super(parent, style);

        super.addPaintListener(this::paint);

        addListener(SWT.MouseDown, event -> {
            final SelectionEvent se = new SelectionEvent(event);
            for (SelectionListener sl : selectors) {
                sl.widgetSelected(se);
            }
        });
    }

    @Override
    @Deprecated
    public void removeListener(int eventType, Listener listener) {
        super.removeListener(eventType, listener);
    }

    @Override
    @Deprecated
    public Listener[] getListeners(int eventType) {
        return super.getListeners(eventType);
    }

    public void clearSelectionListeners() {
        selectors.clear();
    }

    public void clearPaintListeners() {
        painters.clear();
    }

    @Override
    public void addPaintListener(PaintListener listener) {
        painters.add(listener);
    }

    public void setImage(Image image) {
        img = image;
    }

    public void addSelectionListener(SelectionAdapter listener) {
        selectors.add(listener);
    }

    public void addSelectionListener(SelectionListener listener) {
        selectors.add(listener);
    }

    private void paint(PaintEvent event) {
        final boolean hasImage = img != null;

        if (hasImage) {

            event.gc.setBackground(SWTResourceManager.getColor(60, 60, 60));
            event.gc.drawRoundRectangle(0, 0, img.getImageData().width + 9, img.getImageData().height + 9, 5, 5);

            event.gc.drawImage(img, 5, 5);
        }
        for (PaintListener pl : painters) {
            pl.paintControl(event);
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {

        if (img != null) {
            Rectangle bounds = img.getBounds();

            // Just return the size of the image
            return new Point(bounds.width + 10, bounds.height + 10);
        } else {
            return new Point(25, 25);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
