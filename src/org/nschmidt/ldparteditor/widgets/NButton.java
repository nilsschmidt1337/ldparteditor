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
import java.util.HashMap;
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
import org.nschmidt.ldparteditor.helpers.WidgetSelectionListener;
import org.nschmidt.ldparteditor.resources.ResourceManager;

/**
 * @author nils
 *
 */
public class NButton extends Canvas {

    private final List<SelectionListener> selectors = new ArrayList<>();
    private final List<PaintListener> painters = new ArrayList<>();
    private final boolean canToggle;
    private final boolean canCheck;
    private final boolean hasBorder;
    private final boolean isRadio;

    private Image img = null;
    private String text = ""; //$NON-NLS-1$
    private boolean hovered = false;
    private boolean pressed = false;
    private boolean selected = false;
    private static HashMap<Composite, ArrayList<NButton>> radioGroups = new HashMap<>();

    public NButton(Composite parent, int style) {
        super(parent, style);

        isRadio = (style & SWT.RADIO) == SWT.RADIO;
        canToggle = (style & SWT.TOGGLE) == SWT.TOGGLE;
        canCheck = (style & SWT.CHECK) == SWT.CHECK;
        hasBorder = (style & SWT.BORDER) == SWT.BORDER;

        if (canCheck) {
            img = ResourceManager.getImage("icon16_unchecked.png"); //$NON-NLS-1$
        }

        if (isRadio) {
            ArrayList<NButton> groups = radioGroups.get(parent);
            if (groups == null) {
                groups = new ArrayList<>();
                radioGroups.put(parent, groups);
            }
            groups.add(this);
        }

        super.addPaintListener(this::paint);

        addListener(SWT.MouseDown, event -> {
            pressed = true;
            if (canToggle || canCheck) {
                setSelection(!selected);
            }
            if (isRadio) {
                setSelection(true);
                for (NButton b : radioGroups.get(parent)) {
                    if (this != b) {
                        b.selected = false;
                        b.redraw();
                    }
                }
            }
            redraw();
            final SelectionEvent se = new SelectionEvent(event);
            for (SelectionListener sl : selectors) {
                sl.widgetSelected(se);
            }
        });
        addListener(SWT.MouseUp, event -> {
            pressed = false;
            redraw();
        });
        addListener(SWT.MouseEnter, event -> {
            pressed = false;
            hovered = true;
            redraw();
        });
        addListener(SWT.MouseExit, event -> {
            pressed = false;
            hovered = false;
            redraw();
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
        checkWidget();
        if (canCheck) return;
        img = image;
    }

    public void addSelectionListener(WidgetSelectionListener selectionListener) {
        selectors.add(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectionListener.widgetSelected(e);
            }
        });
    }

    public void addSelectionListener(SelectionListener listener) {
        selectors.add(listener);
    }

    private void paint(PaintEvent event) {
        final GC gc = event.gc;
        final Image img = this.img;
        final boolean focused = this.isFocusControl();
        final boolean enabled = this.isEnabled();
        final boolean hasImage = img != null;
        final int img_width = hasImage ? img.getImageData().width : 0;
        final int img_height = hasImage ? img.getImageData().height : 0;
        final int this_width = getBounds().width - 1;

        gc.setFont(Font.SYSTEM);
        // setFont before using textExtent, so that the size of the text
        // can be calculated correctly
        final Point textExtent = getText().isEmpty() ? new Point(0,0) : gc.textExtent(getText());

        // TODO 1. Calculate sizes


        // TODO 2. Draw Content

        if (selected && (canToggle || isRadio)) {
            gc.setBackground(SWTResourceManager.getColor(160, 160, 200));
            gc.fillRoundRectangle(0, 0, Math.max(img_width + 9 + textExtent.x, this_width), Math.max(textExtent.y, img_height) + 9, 5, 5);
            gc.setBackground(getBackground());
        }

        gc.setForeground(SWTResourceManager.getColor(255, 255, 255));

        if (hovered || focused) {
            gc.setForeground(SWTResourceManager.getColor(0, 0, 0));
        }
        if (pressed) {
            gc.setForeground(SWTResourceManager.getColor(220, 220, 220));
        }



        if (!canCheck && !hasBorder) {

            if (!enabled) {
                gc.setForeground(SWTResourceManager.getColor(200, 200, 200));
            }
            gc.drawRoundRectangle(0, 0, Math.max(img_width + 9 + textExtent.x, this_width), Math.max(textExtent.y, img_height) + 9, 5, 5);

            gc.setForeground(SWTResourceManager.getColor(60, 60, 60));

            if (hovered || focused) {
                gc.setBackground(SWTResourceManager.getColor(160, 160, 200));
                gc.fillRoundRectangle(1, 1, Math.max(img_width + 9 + textExtent.x, this_width) - 1, Math.max(textExtent.y, img_height) + 9 - 1, 5, 5);
                if (selected && (canToggle || isRadio)) {
                    gc.setBackground(SWTResourceManager.getColor(160, 160, 200));
                } else {
                    gc.setBackground(getBackground());
                }
                gc.fillRoundRectangle(2, 2, Math.max(img_width + 9 + textExtent.x, this_width) - 3, Math.max(textExtent.y, img_height) + 9 - 3, 5, 5);

            }
            if (pressed) {
                gc.setForeground(SWTResourceManager.getColor(30, 30, 30));
            }
            if (!enabled) {
                gc.setForeground(SWTResourceManager.getColor(200, 200, 200));
            }
            gc.drawRoundRectangle(1, 1, Math.max(img_width + 9 + textExtent.x, this_width) - 1, Math.max(textExtent.y, img_height) + 9 - 1, 5, 5);
        }

        if (hasImage) {

            gc.drawImage(img, 5, 5);
        }

        gc.setForeground(getForeground());

        gc.drawString(getText(), img_width + 5, 5, true);


        // 3. Paint custom forms
        for (PaintListener pl : painters) {
            pl.paintControl(event);
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint) {
        return computeSize(wHint, hHint, false);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {

        final GC gc = new GC(this);
        gc.setFont(Font.SYSTEM);
        // setFont before using textExtent, so that the size of the text
        // can be calculated correctly
        final Point textExtent = getText().isEmpty() ? new Point(0,0) : gc.textExtent(getText());
        gc.dispose();

        if (img != null) {
            ImageData data = img.getImageData();

            // Just return the size of the image
            return new Point(data.width + textExtent.x + 10, Math.max(data.height, textExtent.y) + 10);
        } else if (!getText().isEmpty()) {
            return new Point(10 + textExtent.x, 10 + textExtent.y);
        } else {
            return new Point(25 + textExtent.x, 25 + textExtent.y);
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
        if (canCheck) {
            if (selected) {
                img = ResourceManager.getImage("icon16_checked.png"); //$NON-NLS-1$
            } else {
                img = ResourceManager.getImage("icon16_unchecked.png"); //$NON-NLS-1$
            }
        }
        redraw();
    }

    public void setText(String text) {
        checkWidget();
        this.text = text;
        redraw();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        checkWidget();
        redraw();
    }
}
