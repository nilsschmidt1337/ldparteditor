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
package org.nschmidt.ldparteditor.composites;

import java.awt.MouseInfo;
import java.awt.PointerInfo;
import java.text.DecimalFormatSymbols;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;
import org.nschmidt.ldparteditor.enums.Font;
import org.nschmidt.ldparteditor.enums.MyLanguage;
import org.nschmidt.ldparteditor.enums.View;
import org.nschmidt.ldparteditor.helpers.Cocoa;
import org.nschmidt.ldparteditor.i18n.I18n;
import org.nschmidt.ldparteditor.resources.ResourceManager;
import org.nschmidt.ldparteditor.widgets.NButton;

/**
 * @author nils
 *
 */
public class CompositeScale extends ScalableComposite {

    private final java.text.DecimalFormat NUMBER_FORMAT2F = new java.text.DecimalFormat(View.NUMBER_FORMAT2F, new DecimalFormatSymbols(MyLanguage.LOCALE));

    private final Canvas canvas_horizontal;
    private final Canvas canvas_vertical;

    private int pos_horizontal;
    private int pos_vertical;

    public CompositeScale(Composite parentCompositeContainer, Composite3D composite3D, int style) {
        super(parentCompositeContainer, style);
        final CompositeScale me = this;
        this.setParent(parentCompositeContainer);
        GridLayout gl_compositeScale = new GridLayout(2, false);
        gl_compositeScale.marginHeight = 0;
        gl_compositeScale.marginWidth = 0;
        gl_compositeScale.verticalSpacing = 0;
        gl_compositeScale.horizontalSpacing = 0;
        this.setLayout(gl_compositeScale);

        NButton btn_openContextMenu = new NButton(this, Cocoa.getStyle());
        GridData gd_btn_openContextMenu = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
        gd_btn_openContextMenu.heightHint = 20;
        gd_btn_openContextMenu.widthHint = 20;
        btn_openContextMenu.setLayoutData(gd_btn_openContextMenu);
        btn_openContextMenu.setImage(ResourceManager.getImage("icon16_contextMenu.png")); //$NON-NLS-1$
        btn_openContextMenu.setBounds(0, 0, 20, 20);

        btn_openContextMenu.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PointerInfo a = MouseInfo.getPointerInfo();
                java.awt.Point b = a.getLocation();
                int x = (int) b.getX();
                int y = (int) b.getY();
                if (!me.getComposite3D().getMenu().isDisposed()) {
                    me.getComposite3D().getMenu().setLocation(x, y);
                    me.getComposite3D().getMenu().setVisible(true);
                }
            }
        });

        canvas_horizontal = new Canvas(this, SWT.BORDER | SWT.NO_BACKGROUND);
        canvas_horizontal.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
        canvas_horizontal.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
        canvas_horizontal.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        canvas_horizontal.setLayout(new FillLayout(SWT.HORIZONTAL));

        Listener paintHorizontalScaleListener = new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (e.gc == null)
                    return;
                Rectangle rect = canvas_horizontal.getBounds();
                // Create the image to fill the canvas
                Image image = new Image(Display.getCurrent(), rect);
                // Set up the offscreen gc
                GC gc = new GC(image);
                // Draw the background
                gc.setBackground(e.gc.getBackground());
                gc.fillRectangle(image.getBounds());

                Composite3D c3d = me.getComposite3D();

                if (c3d.isClassicPerspective()) {
                    float width = rect.width;
                    float halfwidth = width / 2f;
                    float offset = 0;
                    float viewport_pixel_per_ldu = c3d.getZoom() * View.PIXEL_PER_LDU;
                    while (viewport_pixel_per_ldu > 10) {
                        viewport_pixel_per_ldu = viewport_pixel_per_ldu / 10f;
                    }
                    while (viewport_pixel_per_ldu < 10) {
                        viewport_pixel_per_ldu = viewport_pixel_per_ldu * 10f;
                    }
                    if (viewport_pixel_per_ldu > 10) {
                        viewport_pixel_per_ldu = viewport_pixel_per_ldu / 2f;
                    }
                    float step = viewport_pixel_per_ldu;
                    int factor = 1;
                    gc.setFont(Font.SMALL);
                    gc.drawText(I18n.UNIT_CurrentUnit(), 0, 0);

                    // Draw the ruler
                    float twostep = step * 2;
                    float tenstep = step * 10;
                    // MARK Ruler Perspective Horizontal
                    switch (c3d.getPerspectiveIndex()) {
                    case FRONT:
                        offset = (int) (c3d.getTranslation().m30 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2;
                        break;
                    case BACK:
                        offset = (int) -(c3d.getTranslation().m30 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2;
                        factor = -1;
                        break;
                    case LEFT:
                        offset = (int) -(c3d.getTranslation().m32 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2;
                        factor = -1;
                        break;
                    case RIGHT:
                        offset = (int) (c3d.getTranslation().m32 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2;
                        break;
                    case TOP:
                        offset = (int) (c3d.getTranslation().m30 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2;
                        break;
                    case BOTTOM:
                        offset = (int) (c3d.getTranslation().m30 * c3d.getZoom() * View.PIXEL_PER_LDU) - 2;
                        break;
                    case TWO_THIRDS:
                        throw new AssertionError();
                    }

                    float scale_factor = factor / (View.PIXEL_PER_LDU * c3d.getZoom() * 1000f) * View.unit_factor.floatValue();

                    if (offset < -halfwidth) {
                        // "Positive"
                        // Origin
                        float origin = (offset + halfwidth) % tenstep;
                        float origin2 = offset + halfwidth;
                        // Big lines
                        for (float x = origin + tenstep; x < width; x = x + tenstep) {
                            if (x > 20) {
                                gc.drawText(NUMBER_FORMAT2F.format((x - origin2) * scale_factor), (int) x + 2, -2);
                                gc.drawLine((int) x, 5, (int) x, 10);
                            }
                        }
                        // Small lines
                        for (float x = origin + step; x < width; x = x + twostep) {
                            if (x > 20) {
                                gc.drawLine((int) x, 13, (int) x, 16);
                            }
                        }
                        // Middle
                        for (float x = origin + twostep; x < width; x = x + twostep) {
                            if (x > 20) {
                                gc.drawLine((int) x, 10, (int) x, 16);
                            }
                        }
                    } else if (offset > halfwidth) {
                        // "Negative"
                        // Origin
                        float origin = width - (halfwidth - offset) % tenstep;
                        float origin2 = offset + halfwidth;
                        // Big lines
                        for (float x = origin - tenstep; x > 20; x = x - tenstep) {
                            gc.drawText(NUMBER_FORMAT2F.format((x - origin2) * scale_factor), (int) x + 2, -2);
                            gc.drawLine((int) x, 5, (int) x, 10);
                        }
                        // Small lines
                        for (float x = origin - step; x > 20; x = x - twostep) {
                            gc.drawLine((int) x, 13, (int) x, 16);
                        }
                        // Middle
                        for (float x = origin - twostep; x > 20; x = x - twostep) {
                            gc.drawLine((int) x, 10, (int) x, 16);
                        }
                    } else {
                        // Both
                        // Origin
                        float origin = offset + halfwidth;
                        if (origin > 22) {
                            int origin_int = (int) origin;
                            gc.drawLine(origin_int - 1, 0, origin_int - 1, 16);
                            gc.drawLine(origin_int, 0, origin_int, 16);
                            gc.drawLine(origin_int + 1, 0, origin_int + 1, 16);
                        }
                        // Big lines
                        for (float x = origin - tenstep; x > 20; x = x - tenstep) {
                            gc.drawText(NUMBER_FORMAT2F.format((x - origin) * scale_factor), (int) x + 2, -2);
                            gc.drawLine((int) x, 5, (int) x, 10);
                        }
                        for (float x = origin + tenstep; x < width; x = x + tenstep) {
                            gc.drawText(NUMBER_FORMAT2F.format((x - origin) * scale_factor), (int) x + 2, -2);
                            gc.drawLine((int) x, 5, (int) x, 10);
                        }
                        // Small lines
                        for (float x = origin - step; x > 20; x = x - twostep) {
                            gc.drawLine((int) x, 13, (int) x, 16);
                        }
                        for (float x = origin + step; x < width; x = x + twostep) {
                            gc.drawLine((int) x, 13, (int) x, 16);
                        }
                        // Middle
                        for (float x = origin - twostep; x > 20; x = x - twostep) {
                            gc.drawLine((int) x, 10, (int) x, 16);
                        }
                        for (float x = origin + twostep; x < width; x = x + twostep) {
                            gc.drawLine((int) x, 10, (int) x, 16);
                        }
                    }
                }
                // Draw the triangle
                gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
                gc.fillPolygon(new int[] { pos_horizontal - 5, 10, pos_horizontal, 16, pos_horizontal + 5, 10, pos_horizontal - 5, 10 });
                // Draw the offscreen buffer to the screen
                e.gc.drawImage(image, 0, 0);
                // Clean up
                image.dispose();
                gc.dispose();
            }
        };
        canvas_horizontal.addListener(SWT.Resize, paintHorizontalScaleListener);
        canvas_horizontal.addListener(SWT.Paint, paintHorizontalScaleListener);

        canvas_vertical = new Canvas(this, SWT.BORDER | SWT.NO_BACKGROUND);
        canvas_vertical.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
        canvas_vertical.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
        canvas_vertical.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1));
        canvas_vertical.setLayout(new FillLayout(SWT.HORIZONTAL));

        Listener paintVerticalScaleListener = new Listener() {
            @Override
            public void handleEvent(Event e) {
                if (e.gc == null)
                    return;
                Rectangle rect = canvas_vertical.getBounds();
                // Create the image to fill the canvas
                Image image = new Image(Display.getCurrent(), rect);
                // Set up the offscreen gc
                GC gc = new GC(image);
                // Draw the background
                gc.setBackground(e.gc.getBackground());
                gc.fillRectangle(image.getBounds());

                Composite3D c3d = me.getComposite3D();

                if (c3d.isClassicPerspective()) {
                    float height = rect.height;
                    float halfheight = height / 2f;
                    float offset = 0;
                    float viewport_pixel_per_ldu = c3d.getZoom() * View.PIXEL_PER_LDU;
                    while (viewport_pixel_per_ldu > 10) {
                        viewport_pixel_per_ldu = viewport_pixel_per_ldu / 10f;
                    }
                    while (viewport_pixel_per_ldu < 10) {
                        viewport_pixel_per_ldu = viewport_pixel_per_ldu * 10f;
                    }
                    if (viewport_pixel_per_ldu > 10) {
                        viewport_pixel_per_ldu = viewport_pixel_per_ldu / 2f;
                    }
                    float step = viewport_pixel_per_ldu;
                    int factor = 1;
                    gc.setFont(Font.SMALL);

                    // Draw the ruler
                    float twostep = step * 2;
                    float tenstep = step * 10;
                    // MARK Ruler Perspective Vertical
                    switch (c3d.getPerspectiveIndex()) {
                    case FRONT:
                        offset = (int) (c3d.getTranslation().m31 * c3d.getZoom() * View.PIXEL_PER_LDU);
                        break;
                    case BACK:
                        offset = (int) (c3d.getTranslation().m31 * c3d.getZoom() * View.PIXEL_PER_LDU);
                        break;
                    case LEFT:
                        offset = (int) (c3d.getTranslation().m31 * c3d.getZoom() * View.PIXEL_PER_LDU);
                        break;
                    case RIGHT:
                        offset = (int) (c3d.getTranslation().m31 * c3d.getZoom() * View.PIXEL_PER_LDU);
                        break;
                    case TOP:
                        offset = (int) -(c3d.getTranslation().m32 * c3d.getZoom() * View.PIXEL_PER_LDU);
                        factor = -1;
                        break;
                    case BOTTOM:
                        offset = (int) (c3d.getTranslation().m32 * c3d.getZoom() * View.PIXEL_PER_LDU);
                        break;
                    case TWO_THIRDS:
                        throw new AssertionError();
                    }

                    float scale_factor = factor / (View.PIXEL_PER_LDU * c3d.getZoom() * 1000f) * View.unit_factor.floatValue();

                    if (offset < -halfheight) {
                        // "Positive"
                        // Origin
                        float origin = (offset + halfheight) % tenstep;
                        float origin2 = offset + halfheight;
                        // Big lines
                        for (float y = origin + tenstep; y < height; y = y + tenstep) {
                            Transform tr = new Transform(Display.getCurrent());
                            tr.translate(0, (int) y);
                            tr.rotate(-90);
                            gc.setTransform(tr);
                            gc.drawText(NUMBER_FORMAT2F.format((y - origin2) * scale_factor), 0, 0);
                            gc.setTransform(null);
                            gc.drawLine(5, (int) y, 10, (int) y);
                        }
                        // Small lines
                        for (float y = origin + step; y < height; y = y + twostep) {
                            gc.drawLine(13, (int) y, 16, (int) y);
                        }
                        // Middle
                        for (float y = origin + twostep; y < height; y = y + twostep) {
                            gc.drawLine(10, (int) y, 16, (int) y);
                        }
                    } else if (offset > halfheight) {
                        // "Negative"
                        // Origin
                        float origin = height - (halfheight - offset) % tenstep;
                        float origin2 = offset + halfheight;
                        // Big lines
                        for (float y = origin - tenstep; y > 0; y = y - tenstep) {
                            Transform tr = new Transform(Display.getCurrent());
                            tr.translate(0, (int) y);
                            tr.rotate(-90);
                            gc.setTransform(tr);
                            gc.drawText(NUMBER_FORMAT2F.format((y - origin2) * scale_factor), 0, 0);
                            gc.setTransform(null);
                            gc.drawLine(5, (int) y, 10, (int) y);
                        }
                        // Small lines
                        for (float y = origin - step; y > 0; y = y - twostep) {
                            gc.drawLine(13, (int) y, 16, (int) y);
                        }
                        // Middle
                        for (float y = origin - twostep; y > 0; y = y - twostep) {
                            gc.drawLine(10, (int) y, 16, (int) y);
                        }
                    } else {
                        // Both
                        // Origin
                        float origin = offset + halfheight;
                        int origin_int = (int) origin;
                        gc.drawLine(0, origin_int - 1, 16, origin_int - 1);
                        gc.drawLine(0, origin_int, 16, origin_int);
                        gc.drawLine(0, origin_int + 1, 16, origin_int + 1);
                        // Big lines
                        for (float y = origin - tenstep; y > 0; y = y - tenstep) {
                            Transform tr = new Transform(Display.getCurrent());
                            tr.translate(0, (int) y);
                            tr.rotate(-90);
                            gc.setTransform(tr);
                            gc.drawText(NUMBER_FORMAT2F.format((y - origin) * scale_factor), 0, 0);
                            gc.setTransform(null);
                            gc.drawLine(5, (int) y, 10, (int) y);
                        }
                        for (float y = origin + tenstep; y < height; y = y + tenstep) {
                            Transform tr = new Transform(Display.getCurrent());
                            tr.translate(0, (int) y);
                            tr.rotate(-90);
                            gc.setTransform(tr);
                            gc.drawText(NUMBER_FORMAT2F.format((y - origin) * scale_factor), 0, 0);
                            gc.setTransform(null);
                            gc.drawLine(5, (int) y, 10, (int) y);
                        }
                        // Small lines
                        for (float y = origin - step; y > 0; y = y - twostep) {
                            gc.drawLine(13, (int) y, 16, (int) y);
                        }
                        for (float y = origin + step; y < height; y = y + twostep) {
                            gc.drawLine(13, (int) y, 16, (int) y);
                        }
                        // Middle
                        for (float y = origin - twostep; y > 0; y = y - twostep) {
                            gc.drawLine(10, (int) y, 16, (int) y);
                        }
                        for (float y = origin + twostep; y < height; y = y + twostep) {
                            gc.drawLine(10, (int) y, 16, (int) y);
                        }
                    }
                }
                // Draw the triangle
                gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_FOREGROUND));
                gc.fillPolygon(new int[] { 10, pos_vertical - 5, 16, pos_vertical, 10, pos_vertical + 5, 10, pos_vertical - 5 });
                // Draw the offscreen buffer to the screen
                e.gc.drawImage(image, 0, 0);
                // Clean up
                image.dispose();
                gc.dispose();
            }
        };
        canvas_vertical.addListener(SWT.Resize, paintVerticalScaleListener);
        canvas_vertical.addListener(SWT.Paint, paintVerticalScaleListener);

        composite3D.setParent(this);
        composite3D.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
    }

    @Override
    public SashForm getSashForm() {
        return (SashForm) this.getParent().getParent();
    }

    @Override
    public Composite3D getComposite3D() {
        return (Composite3D) this.getChildren()[3];
    }

    @Override
    public CompositeContainer getCompositeContainer() {
        return ((ScalableComposite) this.getParent()).getCompositeContainer();
    }

    @Override
    public void redrawScales(int x, int y) {
        pos_horizontal = x - 2;
        pos_vertical = y - 2;
        canvas_horizontal.redraw();
        canvas_horizontal.update();
        canvas_vertical.redraw();
        canvas_vertical.update();
    }

    @Override
    public void redrawScales() {
        canvas_horizontal.redraw();
        canvas_horizontal.update();
        canvas_vertical.redraw();
        canvas_vertical.update();
    }
}
