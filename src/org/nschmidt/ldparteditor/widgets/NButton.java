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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.enums.Font;

/**
 * @author nils
 *
 */
public class NButton extends Canvas {

    private final List<SelectionListener> selectors = new ArrayList<>();
    private final List<PaintListener> painters = new ArrayList<>();
    private final boolean canToggle;


    private Image img = null;
    private String text = ""; //$NON-NLS-1$
    private boolean hovered = false;
    private boolean pressed = false;
    private boolean selected = false;


    public NButton(Composite parent, int style) {
        super(parent, style);

        canToggle = (style & SWT.TOGGLE) == SWT.TOGGLE;

        super.addPaintListener(this::paint);

        addListener(SWT.MouseDown, event -> {
            pressed = true;
            if (canToggle) {
                selected = !selected;
            }
            redraw();
            update();
            final SelectionEvent se = new SelectionEvent(event);
            for (SelectionListener sl : selectors) {
                sl.widgetSelected(se);
            }
        });
        addListener(SWT.MouseUp, event -> {
            pressed = false;
            redraw();
            update();
        });
        addListener(SWT.MouseEnter, event -> {
            hovered = true;
            redraw();
            update();
        });
        addListener(SWT.MouseExit, event -> {
            hovered = false;
            redraw();
            update();
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
        final GC gc = event.gc;
        final Image img = this.img;
        final boolean focused = this.isFocusControl();
        final boolean hasImage = img != null;
        final int img_width = hasImage ? img.getImageData().width : 0;
        final int img_height = hasImage ? img.getImageData().height : 0;


        gc.setFont(Font.SYSTEM);
        // setFont before using textExtent, so that the size of the text
        // can be calculated correctly
        final Point textExtent = gc.textExtent(getText());

        // TODO 1. Calculate sizes


        // TODO 2. Draw Content


        gc.setForeground(SWTResourceManager.getColor(60, 60, 60));

        if (hovered) {
            gc.setForeground(SWTResourceManager.getColor(255, 255, 255));
        }
        if (pressed) {
            gc.setForeground(SWTResourceManager.getColor(30, 30, 30));
        }

        gc.drawRoundRectangle(0, 0, img_width + 9, img_height + 9, 5, 5);

        if (hasImage) {

            gc.drawImage(img, 5, 5);
        }

        gc.setForeground(getForeground());

        gc.drawString(getText(), 0, 0, true);


        // 3. Paint custom forms
        for (PaintListener pl : painters) {
            pl.paintControl(event);
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {

        final GC gc = new GC(this);
        gc.setFont(Font.SYSTEM);
        // setFont before using textExtent, so that the size of the text
        // can be calculated correctly
        final Point textExtent = gc.textExtent(getText());
        gc.dispose();

        if (img != null) {
            ImageData data = img.getImageData();

            // Just return the size of the image
            return new Point(data.width + 10, data.height + 10);
        } else {
            return new Point(25, 25);
        }
    }

    public String getText() {
        checkWidget();
        return text;
    }

    public boolean getSelection() {
        checkWidget();
        return selected;
    }

    public void setSelection(boolean selected) {
        checkWidget();
        this.selected = selected;
        redraw();
    }

    public void setText(String text) {
        checkWidget();
        this.text = text;
        redraw();
    }
}
